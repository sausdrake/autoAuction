package com.example.autoauction.vehicle.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRejectRequest(
        @NotNull Long vehicleId,
        @NotNull Long adminId,
        @NotBlank String adminName,
        @NotBlank String reason
) {}