package com.example.autoauction.admin.web;

import com.example.autoauction.vehicle.application.VehicleService;
import com.example.autoauction.vehicle.domain.VehicleStatus;
import com.example.autoauction.vehicle.web.dto.VehicleApproveRequest;
import com.example.autoauction.vehicle.web.dto.VehicleRejectRequest;
import com.example.autoauction.vehicle.web.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vehicles")
@Tag(name = "Администрирование автомобилей", description = "Управление отчетами об автомобилях (для администраторов)")
public class AdminVehicleController {

    private final VehicleService vehicleService;

    public AdminVehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/pending")
    @Operation(summary = "Получить отчеты на проверке")
    public List<VehicleResponse> getPendingVehicles() {
        return vehicleService.getVehiclesByStatus(VehicleStatus.PENDING_REVIEW);
    }

    @GetMapping("/needs-fixes")
    @Operation(summary = "Получить отчеты, требующие доработки")
    public List<VehicleResponse> getNeedsFixesVehicles() {
        return vehicleService.getVehiclesByStatus(VehicleStatus.NEEDS_FIXES);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить детали отчета")
    public ResponseEntity<VehicleResponse> getVehicleDetails(@PathVariable Long id) {
        VehicleResponse response = vehicleService.getVehicleDetails(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Одобрить отчет")
    public ResponseEntity<VehicleResponse> approveVehicle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long adminId = 1L; // TODO: получить из SecurityContext
        String adminName = currentUser.getUsername();

        VehicleResponse response = vehicleService.approveVehicle(id, adminId, adminName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Отклонить отчет")
    public ResponseEntity<VehicleResponse> rejectVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRejectRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long adminId = 1L; // TODO: получить из SecurityContext
        String adminName = currentUser.getUsername();

        VehicleResponse response = vehicleService.rejectVehicle(id, adminId, adminName, request.reason());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approved")
    @Operation(summary = "Получить одобренные отчеты")
    public List<VehicleResponse> getApprovedVehicles() {
        return vehicleService.getVehiclesByStatus(VehicleStatus.APPROVED);
    }

    @GetMapping("/in-auction")
    @Operation(summary = "Получить отчеты в аукционе")
    public List<VehicleResponse> getInAuctionVehicles() {
        return vehicleService.getVehiclesByStatus(VehicleStatus.IN_AUCTION);
    }
}