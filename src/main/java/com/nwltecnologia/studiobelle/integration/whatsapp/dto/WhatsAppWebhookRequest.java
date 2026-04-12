package com.nwltecnologia.studiobelle.integration.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;

public record WhatsAppWebhookRequest(
        @NotBlank String tenantId,
        @NotBlank String fromPhone,
        @NotBlank String message
) {
}
