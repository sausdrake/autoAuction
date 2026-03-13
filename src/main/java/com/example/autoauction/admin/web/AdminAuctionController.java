package com.example.autoauction.admin.web;

import com.example.autoauction.admin.application.AdminAuctionService;
import com.example.autoauction.auction.web.dto.AuctionCreateRequest;
import com.example.autoauction.auction.web.dto.AuctionResponse;
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
@RequestMapping("/api/admin/auctions")
@Tag(name = "Администрирование аукционов", description = "Управление аукционами (только для администраторов)")
public class AdminAuctionController {

    private final AdminAuctionService adminAuctionService;

    public AdminAuctionController(AdminAuctionService adminAuctionService) {
        this.adminAuctionService = adminAuctionService;
    }

    @PostMapping
    @Operation(summary = "Создать новый аукцион")
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody AuctionCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        // TODO: получить реальный ID администратора из SecurityContext
        Long adminId = 1L;

        AuctionResponse response = adminAuctionService.createAuction(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Получить все аукционы")
    public List<AuctionResponse> getAllAuctions() {
        return adminAuctionService.getAllAuctions();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить аукцион по ID")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable Long id) {
        AuctionResponse response = adminAuctionService.getAuction(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Запустить аукцион")
    public ResponseEntity<AuctionResponse> startAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        // TODO: получить реальный ID администратора из SecurityContext
        Long adminId = 1L;

        AuctionResponse response = adminAuctionService.startAuction(id, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Отменить аукцион")
    public ResponseEntity<AuctionResponse> cancelAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        // TODO: получить реальный ID администратора из SecurityContext
        Long adminId = 1L;

        AuctionResponse response = adminAuctionService.cancelAuction(id, adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Получить аукционы по статусу")
    public List<AuctionResponse> getAuctionsByStatus(@PathVariable String status) {
        return adminAuctionService.getAuctionsByStatus(status);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Получить аукционы по ID автомобиля")
    public List<AuctionResponse> getAuctionsByVehicleId(@PathVariable Long vehicleId) {
        return adminAuctionService.getAuctionsByVehicleId(vehicleId);
    }

    @GetMapping("/approved-vehicles")
    @Operation(summary = "Получить список автомобилей готовых к аукциону")
    public List<VehicleResponse> getApprovedVehicles() {
        // Этот метод можно добавить позже для удобства
        return List.of();
    }
}