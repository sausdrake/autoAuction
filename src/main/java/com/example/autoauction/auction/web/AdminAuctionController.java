package com.example.autoauction.auction.web;

import com.example.autoauction.auction.application.AdminAuctionService;
import com.example.autoauction.auction.domain.AuctionStatus;
import com.example.autoauction.auction.web.dto.AuctionCreateRequest;
import com.example.autoauction.auction.web.dto.AuctionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/auctions")
@Tag(name = "Администрирование аукционов", description = "Управление аукционами (только для администраторов)")
public class AdminAuctionController {

    private final AdminAuctionService auctionService;

    public AdminAuctionController(AdminAuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping
    @Operation(summary = "Создать новый аукцион")
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody AuctionCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        // TODO: Получить реальный ID админа
        Long adminId = 1L; // Временно

        AuctionResponse response = auctionService.createAuction(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Получить все аукционы")
    public List<AuctionResponse> getAllAuctions() {
        return auctionService.getAllAuctions();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить аукцион по ID")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuction(id));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Запустить аукцион")
    public ResponseEntity<AuctionResponse> startAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long adminId = 1L; // Временно
        AuctionResponse response = auctionService.startAuction(id, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Отменить аукцион")
    public ResponseEntity<AuctionResponse> cancelAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long adminId = 1L; // Временно
        AuctionResponse response = auctionService.cancelAuction(id, adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Получить аукционы по статусу")
    public List<AuctionResponse> getAuctionsByStatus(@PathVariable AuctionStatus status) {
        return auctionService.getAuctionsByStatus(status);
    }
}