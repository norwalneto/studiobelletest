package com.nwltecnologia.studiobelle.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyProfileRequest(
        @NotBlank @Size(min = 2, max = 160) String name,
        @Size(max = 40) String phone,
        @Size(max = 40) String whatsapp,
        @Size(max = 255) String address,
        @NotBlank @Size(min = 5, max = 255) String businessHours
) {
}
