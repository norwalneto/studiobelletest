package com.nwltecnologia.studiobelle.professional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ProfessionalRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotBlank @Size(min = 5, max = 255) String workingHours,
        @NotEmpty Set<Long> serviceIds
) {
}
