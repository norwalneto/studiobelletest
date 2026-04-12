package com.nwltecnologia.studiobelle.professional.dto;

import java.time.Instant;
import java.util.Set;

public record ProfessionalResponse(
        Long id,
        String tenantId,
        String name,
        String workingHours,
        Set<Long> serviceIds,
        Instant createdAt,
        Instant updatedAt
) {
}
