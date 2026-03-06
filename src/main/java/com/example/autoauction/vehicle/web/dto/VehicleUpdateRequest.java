package com.example.autoauction.vehicle.web.dto;

import com.example.autoauction.vehicle.domain.FuelType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record VehicleUpdateRequest(
        // Только поля, которые можно менять
        String licensePlate,
        String color,
        @Min(0) Integer mileage,
        @DecimalMin("0.0") Double engineCapacity,
        FuelType fuelType,
        String transmission,
        String description
) {}