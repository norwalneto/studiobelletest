package com.nwltecnologia.studiobelle.servicecatalog.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceOfferingResponse(
        Long id,
        String tenantId,
        String name,
        Integer durationMinutes,
        BigDecimal price,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
