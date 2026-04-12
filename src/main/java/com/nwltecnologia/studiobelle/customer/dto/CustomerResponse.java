package com.nwltecnologia.studiobelle.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String tenantId,
        String name,
        String phone,
        String email,
        LocalDateTime lastVisit,
        String frequency,
        BigDecimal totalSpent,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
