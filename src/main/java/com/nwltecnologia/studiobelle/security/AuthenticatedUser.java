package com.nwltecnologia.studiobelle.security;

import com.nwltecnologia.studiobelle.user.entity.UserRole;

public record AuthenticatedUser(
        Long id,
        String email,
        String tenantId,
        UserRole role
) {
}
