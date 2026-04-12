package com.nwltecnologia.studiobelle.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record CustomerHistoryResponse(
        Long id,
        Long customerId,
        String serviceName,
        BigDecimal amount,
        LocalDateTime attendedAt,
        String notes,
        Instant createdAt
) {
}
