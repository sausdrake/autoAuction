package com.example.autoauction.vehicle.application;

import com.example.autoauction.vehicle.domain.Vehicle;
import com.example.autoauction.vehicle.domain.VehicleStatus;
import com.example.autoauction.vehicle.domain.port.VehicleRepository;
import com.example.autoauction.vehicle.web.dto.VehicleCreateRequest;
import com.example.autoauction.vehicle.web.dto.VehicleUpdateRequest;
import com.example.autoauction.vehicle.web.dto.VehicleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public VehicleResponse createDraft(VehicleCreateRequest request, Long diagnosticId, String diagnosticName) {
        // Проверяем уникальность VIN
        if (vehicleRepository.existsByVin(request.vin())) {
            throw new IllegalArgumentException("Автомобиль с таким VIN уже существует");
        }

        Vehicle vehicle = new Vehicle(
                request.brand(),
                request.model(),
                request.year(),
                request.vin(),
                request.type(),
                diagnosticId,
                diagnosticName
        );

        // Заполняем опциональные поля
        vehicle.setLicensePlate(request.licensePlate());
        vehicle.setColor(request.color());
        vehicle.setMileage(request.mileage());
        vehicle.setEngineCapacity(request.engineCapacity());
        vehicle.setFuelType(request.fuelType());
        vehicle.setTransmission(request.transmission());
        vehicle.setDescription(request.description());

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getMyVehicles(Long diagnosticId, VehicleStatus status) {
        if (status != null) {
            return vehicleRepository.findByDiagnosticIdAndStatus(diagnosticId, status).stream()
                    .map(VehicleResponse::fromDomain)
                    .collect(Collectors.toList());
        }
        return vehicleRepository.findByDiagnosticId(diagnosticId).stream()
                .map(VehicleResponse::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(Long id, Long diagnosticId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден: " + id));

        // Проверяем, что автомобиль принадлежит диагностику
        if (!vehicle.getDiagnosticId().equals(diagnosticId)) {
            throw new SecurityException("У вас нет доступа к этому автомобилю");
        }

        return VehicleResponse.fromDomain(vehicle);
    }

    @Transactional
    public VehicleResponse updateDraft(Long id, VehicleUpdateRequest request, Long diagnosticId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден: " + id));

        // Проверяем права доступа
        if (!vehicle.getDiagnosticId().equals(diagnosticId)) {
            throw new SecurityException("У вас нет доступа к этому автомобилю");
        }

        // Проверяем статус
        if (vehicle.getStatus() != VehicleStatus.DRAFT && vehicle.getStatus() != VehicleStatus.NEEDS_FIXES) {
            throw new IllegalStateException("Нельзя редактировать автомобиль в статусе: " + vehicle.getStatus());
        }

        // Обновляем только те поля, которые пришли в запросе
        if (request.licensePlate() != null) vehicle.setLicensePlate(request.licensePlate());
        if (request.color() != null) vehicle.setColor(request.color());
        if (request.mileage() != null) vehicle.setMileage(request.mileage());
        if (request.engineCapacity() != null) vehicle.setEngineCapacity(request.engineCapacity());
        if (request.fuelType() != null) vehicle.setFuelType(request.fuelType());
        if (request.transmission() != null) vehicle.setTransmission(request.transmission());
        if (request.description() != null) vehicle.setDescription(request.description());

        vehicle.setUpdatedAt(OffsetDateTime.now());
        Vehicle updated = vehicleRepository.save(vehicle);
        return VehicleResponse.fromDomain(updated);
    }

    @Transactional
    public VehicleResponse submitForReview(Long id, Long diagnosticId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден: " + id));

        if (!vehicle.getDiagnosticId().equals(diagnosticId)) {
            throw new SecurityException("У вас нет доступа к этому автомобилю");
        }

        vehicle.submitForReview();
        Vehicle updated = vehicleRepository.save(vehicle);
        return VehicleResponse.fromDomain(updated);
    }

    @Transactional
    public void deleteDraft(Long id, Long diagnosticId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден: " + id));

        if (!vehicle.getDiagnosticId().equals(diagnosticId)) {
            throw new SecurityException("У вас нет доступа к этому автомобилю");
        }

        if (vehicle.getStatus() != VehicleStatus.DRAFT) {
            throw new IllegalStateException("Можно удалить только черновик");
        }

        vehicleRepository.deleteById(id);
    }

    // ========== МЕТОДЫ ДЛЯ АДМИНА ==========

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status).stream()
                .map(VehicleResponse::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByStatuses(List<VehicleStatus> statuses) {
        return vehicleRepository.findByStatusIn(statuses).stream()
                .map(VehicleResponse::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleDetails(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден: " + id));
        return VehicleResponse.fromDomain(vehicle);
    }

    @Transactional
    public VehicleResponse approveVehicle(Long vehicleId, Long adminId, String adminName) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден: " + vehicleId));

        vehicle.approve(adminId, adminName);
        Vehicle updated = vehicleRepository.save(vehicle);
        return VehicleResponse.fromDomain(updated);
    }

    @Transactional
    public VehicleResponse rejectVehicle(Long vehicleId, Long adminId, String adminName, String reason) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден: " + vehicleId));

        vehicle.reject(adminId, adminName, reason);
        Vehicle updated = vehicleRepository.save(vehicle);
        return VehicleResponse.fromDomain(updated);
    }
}