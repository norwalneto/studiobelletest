package com.nwltecnologia.studiobelle.user.dto;

import com.nwltecnologia.studiobelle.user.entity.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        String tenantId,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
