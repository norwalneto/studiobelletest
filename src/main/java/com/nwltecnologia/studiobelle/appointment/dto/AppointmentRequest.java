package com.nwltecnologia.studiobelle.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull Long ownerUserId,
        @NotBlank @Size(min = 2, max = 120) String clientName,
        @NotBlank @Size(min = 8, max = 40) String clientPhone,
        @NotBlank @Size(min = 3, max = 400) String serviceDescription,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @Future LocalDateTime endTime
) {
}
