package com.nwltecnologia.studiobelle.integration.whatsapp.service;

import com.nwltecnologia.studiobelle.appointment.dto.AppointmentRequest;
import com.nwltecnologia.studiobelle.appointment.dto.AppointmentResponse;
import com.nwltecnologia.studiobelle.appointment.service.AppointmentService;
import com.nwltecnologia.studiobelle.common.exception.BusinessException;
import com.nwltecnologia.studiobelle.customer.entity.Customer;
import com.nwltecnologia.studiobelle.customer.service.CustomerService;
import com.nwltecnologia.studiobelle.integration.openai.OpenAiService;
import com.nwltecnologia.studiobelle.integration.whatsapp.dto.WhatsAppWebhookRequest;
import com.nwltecnologia.studiobelle.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
public class WhatsAppWebhookService {

    private final OpenAiService openAiService;
    private final CustomerService customerService;
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public WhatsAppWebhookService(OpenAiService openAiService,
                                  CustomerService customerService,
                                  AppointmentService appointmentService,
                                  UserRepository userRepository) {
        this.openAiService = openAiService;
        this.customerService = customerService;
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    public AppointmentResponse process(WhatsAppWebhookRequest request) {
        Map<String, String> data = openAiService.extractAppointmentData(request.message());
        Customer customer = customerService.createOrUpdateByPhone(request.tenantId(), request.fromPhone(), data.get("nome"));
        Long ownerId = userRepository.findAllByTenantId(request.tenantId()).stream().findFirst()
                .orElseThrow(() -> new BusinessException("Nenhum usuário encontrado para o tenant"))
                .getId();

        LocalDate date = LocalDate.parse(data.getOrDefault("data", LocalDate.now().plusDays(1).toString()));
        LocalTime time = LocalTime.parse(data.getOrDefault("hora", "10:00"));
        LocalDateTime start = LocalDateTime.of(date, time);

        AppointmentRequest appointmentRequest = new AppointmentRequest(ownerId, customer.getName(), customer.getPhone(),
                data.getOrDefault("servico", "Atendimento"), start, start.plusMinutes(60), customer.getId(), null, null);

        return appointmentService.create(appointmentRequest);
    }
}
