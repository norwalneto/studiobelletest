package com.nwltecnologia.studiobelle.integration.whatsapp.controller;

import com.nwltecnologia.studiobelle.appointment.dto.AppointmentResponse;
import com.nwltecnologia.studiobelle.integration.whatsapp.dto.WhatsAppWebhookRequest;
import com.nwltecnologia.studiobelle.integration.whatsapp.service.WhatsAppWebhookService;
import com.nwltecnologia.studiobelle.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whatsapp/webhook")
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService service;

    public WhatsAppWebhookController(WhatsAppWebhookService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> receive(@Valid @RequestBody WhatsAppWebhookRequest request) {
        try {
            TenantContext.setTenant(request.tenantId());
            return ResponseEntity.ok(service.process(request));
        } finally {
            TenantContext.clear();
        }
    }
}
