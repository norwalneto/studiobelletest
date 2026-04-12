package com.nwltecnologia.studiobelle.company.dto;

import java.time.Instant;

public record CompanyProfileResponse(
        Long id,
        String tenantId,
        String name,
        String phone,
        String whatsapp,
        String address,
        String businessHours,
        Instant createdAt,
        Instant updatedAt
) {
}
