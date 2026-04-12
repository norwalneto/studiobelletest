package com.nwltecnologia.studiobelle.automation.service;

import com.nwltecnologia.studiobelle.automation.dto.AutomationRequest;
import com.nwltecnologia.studiobelle.automation.dto.AutomationResponse;
import com.nwltecnologia.studiobelle.automation.entity.Automation;
import com.nwltecnologia.studiobelle.automation.entity.AutomationType;
import com.nwltecnologia.studiobelle.automation.entity.TenantSettings;
import com.nwltecnologia.studiobelle.automation.repository.AutomationRepository;
import com.nwltecnologia.studiobelle.automation.repository.TenantSettingsRepository;
import com.nwltecnologia.studiobelle.customer.entity.Customer;
import com.nwltecnologia.studiobelle.customer.repository.CustomerRepository;
import com.nwltecnologia.studiobelle.integration.openai.OpenAiService;
import com.nwltecnologia.studiobelle.integration.whatsapp.WhatsAppClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AutomationService {

    private final AutomationRepository automationRepository;
    private final TenantSettingsRepository settingsRepository;
    private final CustomerRepository customerRepository;
    private final OpenAiService openAiService;
    private final WhatsAppClient whatsAppClient;
    private final String phoneNumberId;

    public AutomationService(AutomationRepository automationRepository,
                             TenantSettingsRepository settingsRepository,
                             CustomerRepository customerRepository,
                             OpenAiService openAiService,
                             WhatsAppClient whatsAppClient,
                             @Value("${app.whatsapp.phone-number-id:}") String phoneNumberId) {
        this.automationRepository = automationRepository;
        this.settingsRepository = settingsRepository;
        this.customerRepository = customerRepository;
        this.openAiService = openAiService;
        this.whatsAppClient = whatsAppClient;
        this.phoneNumberId = phoneNumberId;
    }

    public List<AutomationResponse> findAllByCurrentTenant(String tenantId) {
        return automationRepository.findAllByTenantIdOrderByTypeAsc(tenantId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AutomationResponse upsert(String tenantId, AutomationRequest request) {
        Automation automation = automationRepository.findByTenantIdAndType(tenantId, request.type())
                .orElseGet(() -> Automation.builder().tenantId(tenantId).type(request.type()).build());
        automation.setActive(request.active());
        automation.setTemplate(request.template());
        return toResponse(automationRepository.save(automation));
    }

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void processReactivationAutomations() {
        if (phoneNumberId == null || phoneNumberId.isBlank()) return;

        List<TenantSettings> allSettings = settingsRepository.findAll();
        for (TenantSettings settings : allSettings) {
            if (!Boolean.TRUE.equals(settings.getAutomationEnabled())) continue;
            Automation reactivation = automationRepository.findByTenantIdAndType(settings.getTenantId(), AutomationType.REACTIVATION)
                    .filter(a -> Boolean.TRUE.equals(a.getActive()))
                    .orElse(null);
            if (reactivation == null) continue;

            LocalDateTime threshold = LocalDateTime.now().minusDays(settings.getInactivityDays());
            List<Customer> inactive = customerRepository.findAllByTenantIdAndLastVisitBefore(settings.getTenantId(), threshold);
            for (Customer customer : inactive) {
                if (customer.getPhone() == null || customer.getPhone().isBlank()) continue;
                String base = reactivation.getTemplate()
                        .replace("{nome}", customer.getName())
                        .replace("{dias}", String.valueOf(settings.getInactivityDays()));
                String finalMessage = settings.getAiEnabled()
                        ? openAiService.humanizeMessage(base)
                        : base;
                whatsAppClient.sendTextMessage(phoneNumberId, customer.getPhone(), finalMessage);
            }
        }
    }

    private AutomationResponse toResponse(Automation automation) {
        return new AutomationResponse(automation.getId(), automation.getTenantId(), automation.getType(), automation.getActive(),
                automation.getTemplate(), automation.getCreatedAt(), automation.getUpdatedAt());
    }
}
