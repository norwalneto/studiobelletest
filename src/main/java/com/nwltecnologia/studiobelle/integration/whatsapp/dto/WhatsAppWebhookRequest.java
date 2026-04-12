package com.nwltecnologia.studiobelle.integration.whatsapp.dto;

public record WhatsAppWebhookRequest(
        String fromPhone,
        String customerName,
        String message
) {
}
