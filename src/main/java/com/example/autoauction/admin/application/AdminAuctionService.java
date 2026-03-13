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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class AdminAuctionService {

    private final AuctionRepository auctionRepository;
    private final VehicleService vehicleService;

    public AdminAuctionService(AuctionRepository auctionRepository, VehicleService vehicleService) {
        this.auctionRepository = auctionRepository;
        this.vehicleService = vehicleService;
        log.info("AdminAuctionService инициализирован");
    }

    @Transactional
    public AuctionResponse createAuction(AuctionCreateRequest request, Long adminId) {
        log.info("=== СОЗДАНИЕ АУКЦИОНА ===");
        log.info("Запрос от adminId: {}, vehicleId: {}", adminId, request.vehicleId());
        log.debug("Детали запроса: startingPrice={}, reservePrice={}, buyNowPrice={}, minBidStep={}",
                request.startingPrice(), request.reservePrice(), request.buyNowPrice(), request.minBidStep());
        log.debug("Время начала: {}, время окончания: {}", request.startTime(), request.endTime());

        // 1. Проверяем, существует ли автомобиль
        log.debug("Шаг 1: Проверка существования автомобиля с ID: {}", request.vehicleId());
        VehicleResponse vehicle;
        try {
            vehicle = vehicleService.getVehicleDetails(request.vehicleId());
            log.debug("Автомобиль найден: {} {} {} (VIN: {})",
                    vehicle.brand(), vehicle.model(), vehicle.year(), vehicle.vin());
        } catch (IllegalArgumentException e) {
            log.error("Автомобиль с ID {} не найден", request.vehicleId());
            throw new IllegalArgumentException("Автомобиль с ID " + request.vehicleId() + " не найден");
        }

        // 2. Проверяем статус автомобиля (должен быть APPROVED)
        log.debug("Шаг 2: Проверка статуса автомобиля. Текущий статус: {}", vehicle.status());
        if (!VehicleStatus.APPROVED.equals(vehicle.status())) {
            log.error("Нельзя создать аукцион для автомобиля в статусе {}. Требуется статус APPROVED",
                    vehicle.status());
            throw new IllegalArgumentException(
                    "Нельзя создать аукцион для автомобиля в статусе " + vehicle.status() +
                            ". Требуется статус APPROVED."
            );
        }
        log.debug("Статус автомобиля APPROVED - ок");

        // 3. Проверяем, нет ли уже активного аукциона для этого авто
        log.debug("Шаг 3: Проверка наличия активного аукциона для vehicleId: {}", request.vehicleId());
        if (auctionRepository.existsByVehicleIdAndStatus(request.vehicleId(), AuctionStatus.ACTIVE)) {
            log.error("Автомобиль уже участвует в активном аукционе (статус ACTIVE)");
            throw new IllegalArgumentException(
                    "Автомобиль уже участвует в активном аукционе (статус ACTIVE)"
            );
        }
        log.debug("Активных аукционов не найдено");

        // 4. Проверяем, нет ли созданного, но неактивного аукциона
        log.debug("Шаг 4: Проверка наличия созданного аукциона для vehicleId: {}", request.vehicleId());
        if (auctionRepository.existsByVehicleIdAndStatus(request.vehicleId(), AuctionStatus.CREATED)) {
            log.error("Для этого автомобиля уже создан аукцион (статус CREATED)");
            throw new IllegalArgumentException(
                    "Для этого автомобиля уже создан аукцион (статус CREATED)"
            );
        }
        log.debug("Созданных аукционов не найдено");

        // 5. Проверяем, не был ли автомобиль уже продан
        log.debug("Шаг 5: Проверка, не был ли автомобиль уже продан");
        if (auctionRepository.existsByVehicleIdAndStatus(request.vehicleId(), AuctionStatus.SOLD)) {
            log.error("Автомобиль уже был продан на аукционе");
            throw new IllegalArgumentException(
                    "Автомобиль уже был продан на аукционе"
            );
        }
        log.debug("Автомобиль не продавался ранее");

        // 6. Валидация цен
        log.debug("Шаг 6: Валидация цен");
        if (request.startingPrice().compareTo(request.reservePrice()) > 0) {
            log.error("Резервная цена ({}) не может быть меньше стартовой ({})",
                    request.reservePrice(), request.startingPrice());
            throw new IllegalArgumentException("Резервная цена не может быть меньше стартовой");
        }
        log.debug("Стартовая цена и резервная цена корректны");

        if (request.buyNowPrice() != null &&
                request.buyNowPrice().compareTo(request.startingPrice()) <= 0) {
            log.error("Цена мгновенной покупки ({}) должна быть больше стартовой ({})",
                    request.buyNowPrice(), request.startingPrice());
            throw new IllegalArgumentException("Цена мгновенной покупки должна быть больше стартовой");
        }
        log.debug("Цена мгновенной покупки корректна");

        BigDecimal minBidStepPercent = request.startingPrice().multiply(new BigDecimal("0.01"));
        if (request.minBidStep().compareTo(minBidStepPercent) < 0) {
            log.error("Шаг ставки ({}) не может быть меньше 1% от стартовой цены ({})",
                    request.minBidStep(), minBidStepPercent);
            throw new IllegalArgumentException("Шаг ставки не может быть меньше 1% от стартовой цены");
        }
        log.debug("Шаг ставки корректен");

        // 7. Проверка времени
        log.debug("Шаг 7: Проверка времени");
        if (request.startTime().isBefore(OffsetDateTime.now())) {
            log.error("Время начала ({}) не может быть в прошлом", request.startTime());
            throw new IllegalArgumentException("Время начала не может быть в прошлом");
        }
        log.debug("Время начала корректно");

        if (request.endTime().isBefore(request.startTime())) {
            log.error("Время окончания ({}) должно быть позже времени начала ({})",
                    request.endTime(), request.startTime());
            throw new IllegalArgumentException("Время окончания должно быть позже времени начала");
        }
        log.debug("Время окончания корректно");

        // 8. Создаём аукцион
        log.debug("Шаг 8: Создание объекта Auction");
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
        log.debug("Информация об автомобиле для кэширования: {}", vehicleInfo);

        log.debug("Шаг 9: Сохранение аукциона в БД");
        Auction saved = auctionRepository.save(auction);
        log.info("Аукцион успешно создан! ID: {}, vehicleId: {}, статус: {}",
                saved.getId(), saved.getVehicleId(), saved.getStatus());

        return AuctionResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAllAuctions() {
        log.debug("=== ПОЛУЧЕНИЕ ВСЕХ АУКЦИОНОВ ===");
        List<AuctionResponse> auctions = auctionRepository.findAll().stream()
                .map(AuctionResponse::fromDomain)
                .collect(Collectors.toList());
        log.debug("Найдено аукционов: {}", auctions.size());
        return auctions;
    }

    @Transactional(readOnly = true)
    public AuctionResponse getAuction(Long id) {
        log.debug("=== ПОЛУЧЕНИЕ АУКЦИОНА ПО ID: {} ===", id);
        return auctionRepository.findById(id)
                .map(auction -> {
                    log.debug("Аукцион найден: ID={}, vehicleId={}, статус={}",
                            auction.getId(), auction.getVehicleId(), auction.getStatus());
                    return AuctionResponse.fromDomain(auction);
                })
                .orElseThrow(() -> {
                    log.error("Аукцион не найден: {}", id);
                    return new IllegalArgumentException("Аукцион не найден: " + id);
                });
    }

    @Transactional
    public AuctionResponse startAuction(Long id, Long adminId) {
        log.info("=== ЗАПУСК АУКЦИОНА ===");
        log.info("ID аукциона: {}, adminId: {}", id, adminId);

        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Аукцион не найден: {}", id);
                    return new IllegalArgumentException("Аукцион не найден: " + id);
                });

        log.debug("Текущий статус аукциона: {}", auction.getStatus());
        auction.start();
        log.info("Аукцион запущен, новый статус: {}", auction.getStatus());

        Auction saved = auctionRepository.save(auction);
        log.debug("Аукцион сохранен в БД");

        return AuctionResponse.fromDomain(saved);
    }

    @Transactional
    public AuctionResponse cancelAuction(Long id, Long adminId) {
        log.info("=== ОТМЕНА АУКЦИОНА ===");
        log.info("ID аукциона: {}, adminId: {}", id, adminId);

        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Аукцион не найден: {}", id);
                    return new IllegalArgumentException("Аукцион не найден: " + id);
                });

        log.debug("Текущий статус аукциона: {}", auction.getStatus());
        auction.cancel();
        log.info("Аукцион отменен, новый статус: {}", auction.getStatus());

        Auction saved = auctionRepository.save(auction);
        log.debug("Аукцион сохранен в БД");

        return AuctionResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAuctionsByStatus(String status) {
        log.debug("=== ПОЛУЧЕНИЕ АУКЦИОНОВ ПО СТАТУСУ: {} ===", status);
        AuctionStatus auctionStatus = AuctionStatus.valueOf(status.toUpperCase());
        List<AuctionResponse> auctions = auctionRepository.findByStatus(auctionStatus).stream()
                .map(AuctionResponse::fromDomain)
                .collect(Collectors.toList());
        log.debug("Найдено аукционов со статусом {}: {}", status, auctions.size());
        return auctions;
    }

    @GetMapping("/approved-vehicles")
    @Operation(summary = "Получить список автомобилей готовых к аукциону")
    public List<VehicleResponse> getApprovedVehicles() {
        log.debug("=== ПОЛУЧЕНИЕ АВТОМОБИЛЕЙ СО СТАТУСОМ APPROVED ===");
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByStatus(VehicleStatus.APPROVED);
        log.debug("Найдено автомобилей: {}", vehicles.size());
        return vehicles;
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAuctionsByVehicleId(Long vehicleId) {
        log.debug("=== ПОЛУЧЕНИЕ АУКЦИОНОВ ПО ID АВТОМОБИЛЯ: {} ===", vehicleId);
        List<AuctionResponse> auctions = auctionRepository.findByVehicleId(vehicleId).stream()
                .map(AuctionResponse::fromDomain)
                .collect(Collectors.toList());
        log.debug("Найдено аукционов для автомобиля {}: {}", vehicleId, auctions.size());
        return auctions;
    }
}