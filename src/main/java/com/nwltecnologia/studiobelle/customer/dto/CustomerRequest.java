package com.nwltecnologia.studiobelle.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotBlank @Size(min = 8, max = 40) String phone,
        @Size(max = 180) String email,
        @Size(max = 500) String notes
) {
}
