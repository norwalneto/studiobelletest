package com.nwltecnologia.studiobelle.automation.dto;

import com.nwltecnologia.studiobelle.automation.entity.AutomationType;

import java.time.Instant;

public record AutomationResponse(
        Long id,
        String tenantId,
        AutomationType type,
        Boolean active,
        String template,
        Instant createdAt,
        Instant updatedAt
) {
}
