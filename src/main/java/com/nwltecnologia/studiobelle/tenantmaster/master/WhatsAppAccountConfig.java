package com.nwltecnologia.studiobelle.tenantmaster.master;

public record WhatsAppAccountConfig(
        String tenantId,
        String phoneNumberId,
        String businessAccountId,
        String accessToken
) {
}
