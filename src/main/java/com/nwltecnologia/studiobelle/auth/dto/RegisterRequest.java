package com.nwltecnologia.studiobelle.auth.dto;

import com.nwltecnologia.studiobelle.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 120) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 80) String password,
        @NotNull UserRole role
) {
}
