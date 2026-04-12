package com.nwltecnologia.studiobelle.appointment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        String tenantId,
        Long ownerUserId,
        String ownerUserName,
        String clientName,
        String clientPhone,
        String serviceDescription,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long customerId,
        Long serviceId,
        Long professionalId,
        BigDecimal servicePrice,
        Instant createdAt,
        Instant updatedAt
) {
}
