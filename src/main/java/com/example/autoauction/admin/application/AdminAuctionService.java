package com.example.autoauction.admin.application;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.AuctionStatus;
import com.example.autoauction.auction.domain.port.AuctionRepository;
import com.example.autoauction.auction.web.dto.AuctionCreateRequest;
import com.example.autoauction.auction.web.dto.AuctionResponse;
import com.example.autoauction.vehicle.application.VehicleService;
import com.example.autoauction.vehicle.domain.VehicleStatus;
import com.example.autoauction.vehicle.web.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminAuctionService {

    private final AuctionRepository auctionRepository;
    private final VehicleService vehicleService;

    public AdminAuctionService(AuctionRepository auctionRepository, VehicleService vehicleService) {
        this.auctionRepository = auctionRepository;
        this.vehicleService = vehicleService;
    }

    @Transactional
    public AuctionResponse createAuction(AuctionCreateRequest request, Long adminId) {
        // 1. Проверяем, существует ли автомобиль
        VehicleResponse vehicle;
        try {
            vehicle = vehicleService.getVehicleDetails(request.vehicleId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Автомобиль с ID " + request.vehicleId() + " не найден");
        }

        // 2. Проверяем статус автомобиля (должен быть APPROVED)
        if (!VehicleStatus.APPROVED.equals(vehicle.status())) {
            throw new IllegalArgumentException(
                    "Нельзя создать аукцион для автомобиля в статусе " + vehicle.status() +
                            ". Требуется статус APPROVED."
            );
        }

        // 3. Проверяем, нет ли уже активного аукциона для этого авто
        if (auctionRepository.existsByVehicleIdAndStatus(request.vehicleId(), AuctionStatus.ACTIVE)) {
            throw new IllegalArgumentException(
                    "Автомобиль уже участвует в активном аукционе (статус ACTIVE)"
            );
        }

        // 4. Проверяем, нет ли созданного, но неактивного аукциона
        if (auctionRepository.existsByVehicleIdAndStatus(request.vehicleId(), AuctionStatus.CREATED)) {
            throw new IllegalArgumentException(
                    "Для этого автомобиля уже создан аукцион (статус CREATED)"
            );
        }

        // 5. Проверяем, не был ли автомобиль уже продан
        if (auctionRepository.existsByVehicleIdAndStatus(request.vehicleId(), AuctionStatus.SOLD)) {
            throw new IllegalArgumentException(
                    "Автомобиль уже был продан на аукционе"
            );
        }

        // 6. Валидация цен
        if (request.startingPrice().compareTo(request.reservePrice()) > 0) {
            throw new IllegalArgumentException("Резервная цена не может быть меньше стартовой");
        }

        if (request.buyNowPrice() != null &&
                request.buyNowPrice().compareTo(request.startingPrice()) <= 0) {
            throw new IllegalArgumentException("Цена мгновенной покупки должна быть больше стартовой");
        }

        if (request.minBidStep().compareTo(request.startingPrice().multiply(new BigDecimal("0.01"))) < 0) {
            throw new IllegalArgumentException("Шаг ставки не может быть меньше 1% от стартовой цены");
        }

        // 7. Проверка времени
        if (request.startTime().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Время начала не может быть в прошлом");
        }

        if (request.endTime().isBefore(request.startTime())) {
            throw new IllegalArgumentException("Время окончания должно быть позже времени начала");
        }

        // 8. Создаём аукцион
        Auction auction = new Auction(
                request.vehicleId(),
                request.startingPrice(),
                request.reservePrice(),
                request.buyNowPrice(),
                request.minBidStep(),
                request.startTime(),
                request.endTime(),
                adminId
        );

        // 9. Сохраняем информацию об автомобиле (для кэширования)
        String vehicleInfo = String.format("%s %s %d (VIN: %s)",
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.vin()
        );
        auction.setVehicleInfo(vehicleInfo);

        Auction saved = auctionRepository.save(auction);
        return AuctionResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(AuctionResponse::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuctionResponse getAuction(Long id) {
        return auctionRepository.findById(id)
                .map(AuctionResponse::fromDomain)
                .orElseThrow(() -> new IllegalArgumentException("Аукцион не найден: " + id));
    }

    @Transactional
    public AuctionResponse startAuction(Long id, Long adminId) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аукцион не найден: " + id));

        auction.start();
        Auction saved = auctionRepository.save(auction);
        return AuctionResponse.fromDomain(saved);
    }

    @Transactional
    public AuctionResponse cancelAuction(Long id, Long adminId) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аукцион не найден: " + id));

        auction.cancel();
        Auction saved = auctionRepository.save(auction);
        return AuctionResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAuctionsByStatus(String status) {
        AuctionStatus auctionStatus = AuctionStatus.valueOf(status.toUpperCase());
        return auctionRepository.findByStatus(auctionStatus).stream()
                .map(AuctionResponse::fromDomain)
                .collect(Collectors.toList());
    }
    // Добавь этот метод в AdminAuctionService.java
    @GetMapping("/approved-vehicles")
    @Operation(summary = "Получить список автомобилей готовых к аукциону")
    public List<VehicleResponse> getApprovedVehicles() {
        return vehicleService.getVehiclesByStatus(VehicleStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAuctionsByVehicleId(Long vehicleId) {
        return auctionRepository.findByVehicleId(vehicleId).stream()
                .map(AuctionResponse::fromDomain)
                .collect(Collectors.toList());
    }
}