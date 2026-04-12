package com.nwltecnologia.studiobelle.appointment.service;

import com.nwltecnologia.studiobelle.appointment.dto.AppointmentRequest;
import com.nwltecnologia.studiobelle.appointment.dto.AppointmentResponse;
import com.nwltecnologia.studiobelle.appointment.entity.Appointment;
import com.nwltecnologia.studiobelle.appointment.repository.AppointmentRepository;
import com.nwltecnologia.studiobelle.integration.openai.OpenAiService;
import com.nwltecnologia.studiobelle.integration.whatsapp.WhatsAppNotificationService;
import com.nwltecnologia.studiobelle.security.ApiSecurityException;
import com.nwltecnologia.studiobelle.tenant.TenantContext;
import com.nwltecnologia.studiobelle.user.entity.User;
import com.nwltecnologia.studiobelle.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final OpenAiService openAiService;
    private final WhatsAppNotificationService whatsAppNotificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              OpenAiService openAiService,
                              WhatsAppNotificationService whatsAppNotificationService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.openAiService = openAiService;
        this.whatsAppNotificationService = whatsAppNotificationService;
    }

    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAllByTenantIdOrderByStartTimeAsc(currentTenant())
                .stream()
                .map(this::toResponse)
                .toList();
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

        ensureNoConflict(tenant, request.startTime(), request.endTime());

        Appointment appointment = Appointment.builder()
                .tenantId(tenant)
                .ownerUser(ownerUser)
                .clientName(request.clientName())
                .clientPhone(request.clientPhone())
                .serviceDescription(request.serviceDescription())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        String smartMessage = openAiService.generateWhatsAppMessage(
                saved.getClientName(),
                saved.getServiceDescription(),
                saved.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
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
        boolean hasConflict = appointmentRepository
                .existsByTenantIdAndStartTimeLessThanAndEndTimeGreaterThan(tenant, end, start);
        if (hasConflict) {
            throw new ApiSecurityException("Já existe agendamento no intervalo informado");
        }
    }

    private boolean hasOverlap(LocalDateTime existingStart, LocalDateTime existingEnd,
                               LocalDateTime requestedStart, LocalDateTime requestedEnd) {
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ApiSecurityException("Horário final deve ser maior que horário inicial");
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
