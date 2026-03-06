package com.example.autoauction.vehicle.web.dto;

import com.example.autoauction.vehicle.domain.FuelType;
import com.example.autoauction.vehicle.domain.VehicleType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record VehicleCreateRequest(
        @NotBlank @Size(min = 2, max = 50) String brand,
        @NotBlank @Size(min = 1, max = 50) String model,
        @NotNull @Min(1900) @Max(2026) Integer year,
        @NotBlank @Size(min = 17, max = 17) String vin,
        @NotNull VehicleType type,

        // Опциональные поля
        String licensePlate,
        String color,
        @Min(0) Integer mileage,
        @DecimalMin("0.0") Double engineCapacity,
        FuelType fuelType,
        String transmission,
        String description
) {}