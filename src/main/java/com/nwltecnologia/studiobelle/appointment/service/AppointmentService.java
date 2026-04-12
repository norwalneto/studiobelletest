package com.nwltecnologia.studiobelle.appointment.service;

import com.nwltecnologia.studiobelle.appointment.dto.AppointmentRequest;
import com.nwltecnologia.studiobelle.appointment.dto.AppointmentResponse;
import com.nwltecnologia.studiobelle.appointment.entity.Appointment;
import com.nwltecnologia.studiobelle.appointment.repository.AppointmentRepository;
import com.nwltecnologia.studiobelle.common.exception.BusinessException;
import com.nwltecnologia.studiobelle.company.repository.CompanyProfileRepository;
import com.nwltecnologia.studiobelle.customer.entity.Customer;
import com.nwltecnologia.studiobelle.customer.service.CustomerService;
import com.nwltecnologia.studiobelle.integration.openai.OpenAiService;
import com.nwltecnologia.studiobelle.integration.whatsapp.WhatsAppNotificationService;
import com.nwltecnologia.studiobelle.professional.entity.Professional;
import com.nwltecnologia.studiobelle.professional.service.ProfessionalService;
import com.nwltecnologia.studiobelle.security.ApiSecurityException;
import com.nwltecnologia.studiobelle.servicecatalog.entity.ServiceOffering;
import com.nwltecnologia.studiobelle.servicecatalog.service.ServiceOfferingService;
import com.nwltecnologia.studiobelle.tenant.TenantContext;
import com.nwltecnologia.studiobelle.user.entity.User;
import com.nwltecnologia.studiobelle.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final OpenAiService openAiService;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final ServiceOfferingService serviceOfferingService;
    private final ProfessionalService professionalService;
    private final CompanyProfileRepository companyProfileRepository;
    private final CustomerService customerService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              OpenAiService openAiService,
                              WhatsAppNotificationService whatsAppNotificationService,
                              ServiceOfferingService serviceOfferingService,
                              ProfessionalService professionalService,
                              CompanyProfileRepository companyProfileRepository,
                              CustomerService customerService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.openAiService = openAiService;
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.serviceOfferingService = serviceOfferingService;
        this.professionalService = professionalService;
        this.companyProfileRepository = companyProfileRepository;
        this.customerService = customerService;
    }

    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAllByTenantIdOrderByStartTimeAsc(currentTenant()).stream().map(this::toResponse).toList();
    }

    public AppointmentResponse findById(Long id) {
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, currentTenant())
                .orElseThrow(() -> new ApiSecurityException("Agendamento não encontrado"));
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        validateTimes(request.startTime(), request.endTime());
        String tenant = currentTenant();
        User ownerUser = userRepository.findByIdAndTenantId(request.ownerUserId(), tenant)
                .orElseThrow(() -> new ApiSecurityException("Usuário responsável não encontrado"));

        ServiceOffering service = null;
        Professional professional = null;
        BigDecimal price = BigDecimal.ZERO;

        if (request.serviceId() != null) {
            service = serviceOfferingService.mustFindByTenant(request.serviceId(), tenant);
            if (!Boolean.TRUE.equals(service.getActive())) {
                throw new BusinessException("Serviço informado está inativo");
            }
            LocalDateTime calculatedEnd = request.startTime().plusMinutes(service.getDurationMinutes());
            if (!calculatedEnd.equals(request.endTime())) {
                throw new BusinessException("Horário final deve respeitar a duração do serviço");
            }
            price = service.getPrice();
        }

        if (request.professionalId() != null) {
            professional = professionalService.mustFindByTenant(request.professionalId(), tenant);
            if (service != null && professional.getServices().stream().noneMatch(s -> s.getId().equals(service.getId()))) {
                throw new BusinessException("Profissional não realiza o serviço informado");
            }
            ensureProfessionalNoConflict(tenant, professional.getId(), request.startTime(), request.endTime());
            validateProfessionalHours(professional.getWorkingHours(), request.startTime(), request.endTime());
        }

        validateCompanyBusinessHours(tenant, request.startTime(), request.endTime());
        ensureNoConflict(tenant, request.startTime(), request.endTime());

        Customer customer = request.customerId() == null ? customerService.createOrUpdateByPhone(tenant, request.clientPhone(), request.clientName())
                : customerService.createOrUpdateByPhone(tenant, request.clientPhone(), request.clientName());

        Appointment appointment = Appointment.builder()
                .tenantId(tenant)
                .ownerUser(ownerUser)
                .customer(customer)
                .service(service)
                .professional(professional)
                .clientName(request.clientName())
                .clientPhone(request.clientPhone())
                .serviceDescription(request.serviceDescription())
                .servicePrice(price)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        customerService.registerAttendance(customer, saved.getServiceDescription(), saved.getServicePrice() == null ? BigDecimal.ZERO : saved.getServicePrice(), saved.getEndTime(), "Atendimento registrado via agenda");

        String smartMessage = openAiService.generateWhatsAppMessage(saved.getClientName(), saved.getServiceDescription(),
                saved.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        whatsAppNotificationService.notifyAppointmentCreated(saved, smartMessage);

        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse update(Long id, AppointmentRequest request) {
        validateTimes(request.startTime(), request.endTime());

        String tenant = currentTenant();
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenant)
                .orElseThrow(() -> new ApiSecurityException("Agendamento não encontrado"));

        User ownerUser = userRepository.findByIdAndTenantId(request.ownerUserId(), tenant)
                .orElseThrow(() -> new ApiSecurityException("Usuário responsável não encontrado"));

        appointmentRepository.findAllByTenantIdOrderByStartTimeAsc(tenant).stream()
                .filter(existing -> !existing.getId().equals(id))
                .filter(existing -> hasOverlap(existing.getStartTime(), existing.getEndTime(), request.startTime(), request.endTime()))
                .findAny()
                .ifPresent(conflict -> {
                    throw new ApiSecurityException("Já existe agendamento no intervalo informado");
                });

        appointment.setOwnerUser(ownerUser);
        appointment.setClientName(request.clientName());
        appointment.setClientPhone(request.clientPhone());
        appointment.setServiceDescription(request.serviceDescription());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public void delete(Long id) {
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, currentTenant())
                .orElseThrow(() -> new ApiSecurityException("Agendamento não encontrado"));
        appointmentRepository.delete(appointment);
    }

    private void ensureNoConflict(String tenant, LocalDateTime start, LocalDateTime end) {
        if (appointmentRepository.existsByTenantIdAndStartTimeLessThanAndEndTimeGreaterThan(tenant, end, start)) {
            throw new ApiSecurityException("Já existe agendamento no intervalo informado");
        }
    }

    private void ensureProfessionalNoConflict(String tenant, Long professionalId, LocalDateTime start, LocalDateTime end) {
        if (appointmentRepository.existsByTenantIdAndProfessionalIdAndStartTimeLessThanAndEndTimeGreaterThan(tenant, professionalId, end, start)) {
            throw new BusinessException("Profissional indisponível no horário informado");
        }
    }

    private boolean hasOverlap(LocalDateTime existingStart, LocalDateTime existingEnd, LocalDateTime requestedStart, LocalDateTime requestedEnd) {
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ApiSecurityException("Horário final deve ser maior que horário inicial");
        }
    }

    private void validateCompanyBusinessHours(String tenant, LocalDateTime start, LocalDateTime end) {
        String hours = companyProfileRepository.findByTenantId(tenant)
                .map(cp -> cp.getBusinessHours())
                .orElse("08:00-18:00");
        validateSimpleHourRange(hours, start, end, "Horário fora do funcionamento da empresa");
    }

    private void validateProfessionalHours(String workingHours, LocalDateTime start, LocalDateTime end) {
        validateSimpleHourRange(workingHours, start, end, "Horário fora da disponibilidade do profissional");
    }

    private void validateSimpleHourRange(String rangeText, LocalDateTime start, LocalDateTime end, String error) {
        String normalized = rangeText.contains(" ") ? rangeText.substring(rangeText.lastIndexOf(' ') + 1) : rangeText;
        if (!normalized.contains("-")) return;
        String[] parts = normalized.split("-");
        LocalTime open = LocalTime.parse(parts[0]);
        LocalTime close = LocalTime.parse(parts[1]);
        if (start.toLocalTime().isBefore(open) || end.toLocalTime().isAfter(close)) {
            throw new BusinessException(error);
        }
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getTenantId(),
                appointment.getOwnerUser().getId(),
                appointment.getOwnerUser().getName(),
                appointment.getClientName(),
                appointment.getClientPhone(),
                appointment.getServiceDescription(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getCustomer() == null ? null : appointment.getCustomer().getId(),
                appointment.getService() == null ? null : appointment.getService().getId(),
                appointment.getProfessional() == null ? null : appointment.getProfessional().getId(),
                appointment.getServicePrice(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }

    private String currentTenant() {
        String tenant = TenantContext.getTenant();
        if (tenant == null || tenant.isBlank()) {
            throw new ApiSecurityException("Tenant não informado");
        }
        return tenant;
    }
}
