package com.example.autoauction.vehicle.web.dto;

import com.example.autoauction.vehicle.domain.FuelType;
import com.example.autoauction.vehicle.domain.Vehicle;
import com.example.autoauction.vehicle.domain.VehicleStatus;
import com.example.autoauction.vehicle.domain.VehicleType;

import java.time.OffsetDateTime;

public record VehicleResponse(
        Long id,
        String brand,
        String model,
        Integer year,
        String vin,
        String licensePlate,
        VehicleType type,
        String color,
        Integer mileage,
        Double engineCapacity,
        FuelType fuelType,
        String transmission,
        String description,
        VehicleStatus status,
        Long diagnosticId,
        String diagnosticName,
        Long adminId,
        String adminName,
        String rejectionReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime submittedAt,
        OffsetDateTime reviewedAt
) {
    public static VehicleResponse fromDomain(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getVin(),
                vehicle.getLicensePlate(),
                vehicle.getType(),
                vehicle.getColor(),
                vehicle.getMileage(),
                vehicle.getEngineCapacity(),
                vehicle.getFuelType(),
                vehicle.getTransmission(),
                vehicle.getDescription(),
                vehicle.getStatus(),
                vehicle.getDiagnosticId(),
                vehicle.getDiagnosticName(),
                vehicle.getAdminId(),
                vehicle.getAdminName(),
                vehicle.getRejectionReason(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt(),
                vehicle.getSubmittedAt(),
                vehicle.getReviewedAt()
        );
    }
}