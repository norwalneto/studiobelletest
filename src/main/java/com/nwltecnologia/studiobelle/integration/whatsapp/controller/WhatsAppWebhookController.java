package com.nwltecnologia.studiobelle.integration.whatsapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.nwltecnologia.studiobelle.appointment.dto.AppointmentResponse;
import com.nwltecnologia.studiobelle.common.exception.BusinessException;
import com.nwltecnologia.studiobelle.integration.whatsapp.dto.WhatsAppWebhookRequest;
import com.nwltecnologia.studiobelle.integration.whatsapp.service.WhatsAppWebhookService;
import com.nwltecnologia.studiobelle.tenant.TenantContext;
import com.nwltecnologia.studiobelle.tenant.TenantResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whatsapp/webhook")
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService service;
    private final TenantResolver tenantResolver;

    public WhatsAppWebhookController(WhatsAppWebhookService service, TenantResolver tenantResolver) {
        this.service = service;
        this.tenantResolver = tenantResolver;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> receive(@RequestBody JsonNode payload) {
        String phoneNumberId = payload.findValue("phone_number_id") == null ? null : payload.findValue("phone_number_id").asText();
        String tenantId = tenantResolver.resolveByPhoneNumberId(phoneNumberId);
        if (tenantId == null) {
            throw new BusinessException("Webhook sem conta WhatsApp vinculada");
        }

        JsonNode firstMessage = payload.findValue("messages");
        JsonNode messageNode = firstMessage != null && firstMessage.isArray() && !firstMessage.isEmpty() ? firstMessage.get(0) : null;
        String from = messageNode == null ? null : messageNode.path("from").asText(null);
        String text = messageNode == null ? null : messageNode.path("text").path("body").asText(null);
        String profileName = payload.findValue("profile") == null ? null : payload.findValue("profile").path("name").asText(null);

        if (from == null || text == null) {
            throw new BusinessException("Payload do webhook sem mensagem de texto");
        }

        try {
            TenantContext.setTenant(tenantId);
            return ResponseEntity.ok(service.process(new WhatsAppWebhookRequest(from, profileName, text)));
        } finally {
            TenantContext.clear();
        }
    }
}
