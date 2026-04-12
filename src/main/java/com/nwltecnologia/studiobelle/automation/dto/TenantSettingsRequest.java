package com.nwltecnologia.studiobelle.automation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TenantSettingsRequest(
        @NotNull @Min(1) @Max(365) Integer inactivityDays,
        @NotNull Boolean aiEnabled,
        @NotNull Boolean automationEnabled
) {
}
