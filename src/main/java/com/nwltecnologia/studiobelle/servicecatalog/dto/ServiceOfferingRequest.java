package com.nwltecnologia.studiobelle.servicecatalog.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServiceOfferingRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotNull @Min(5) @Max(480) Integer durationMinutes,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull Boolean active
) {
}
