package com.nwltecnologia.studiobelle.appointment.dto;

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
        Instant createdAt,
        Instant updatedAt
) {
}
