package com.example.autoauction.vehicle.web;

import com.example.autoauction.vehicle.application.VehicleService;
import com.example.autoauction.vehicle.domain.VehicleStatus;
import com.example.autoauction.vehicle.web.dto.VehicleCreateRequest;
import com.example.autoauction.vehicle.web.dto.VehicleUpdateRequest;
import com.example.autoauction.vehicle.web.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diagnostic/vehicles")
@Tag(name = "Диагностик", description = "Управление отчетами об автомобилях (для диагностиков)")
public class DiagnosticVehicleController {

    private final VehicleService vehicleService;

    public DiagnosticVehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(summary = "Создать черновик отчета")
    public ResponseEntity<VehicleResponse> createDraft(
            @Valid @RequestBody VehicleCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        // TODO: получить реальные данные диагностика из SecurityContext
        Long diagnosticId = 1L;
        String diagnosticName = currentUser.getUsername();

        VehicleResponse response = vehicleService.createDraft(request, diagnosticId, diagnosticName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои отчеты")
    public List<VehicleResponse> getMyVehicles(
            @RequestParam(required = false) VehicleStatus status,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long diagnosticId = 1L; // TODO: получить из SecurityContext
        return vehicleService.getMyVehicles(diagnosticId, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить отчет по ID")
    public ResponseEntity<VehicleResponse> getVehicle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long diagnosticId = 1L; // TODO: получить из SecurityContext
        VehicleResponse response = vehicleService.getVehicle(id, diagnosticId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить черновик (только изменяемые поля)")
    public ResponseEntity<VehicleResponse> updateDraft(
            @PathVariable Long id,
            @Valid @RequestBody VehicleUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long diagnosticId = 1L; // TODO: получить из SecurityContext
        VehicleResponse response = vehicleService.updateDraft(id, request, diagnosticId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Отправить на проверку")
    public ResponseEntity<VehicleResponse> submitForReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long diagnosticId = 1L; // TODO: получить из SecurityContext
        VehicleResponse response = vehicleService.submitForReview(id, diagnosticId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить черновик")
    public ResponseEntity<Void> deleteDraft(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long diagnosticId = 1L; // TODO: получить из SecurityContext
        vehicleService.deleteDraft(id, diagnosticId);
        return ResponseEntity.noContent().build();
    }
}