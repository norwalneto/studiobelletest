package com.nwltecnologia.studiobelle.automation.dto;

import com.nwltecnologia.studiobelle.automation.entity.AutomationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AutomationRequest(
        @NotNull AutomationType type,
        @NotNull Boolean active,
        @NotBlank @Size(min = 5, max = 500) String template
) {
}
