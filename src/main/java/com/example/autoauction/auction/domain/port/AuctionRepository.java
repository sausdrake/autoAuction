package com.example.autoauction.auction.domain.port;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.AuctionStatus;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository {
    // Базовые CRUD операции
    Auction save(Auction auction);
    Optional<Auction> findById(Long id);
    List<Auction> findAll();
    void deleteById(Long id);

    // Поиск по статусу
    List<Auction> findByStatus(AuctionStatus status);

    // Поиск по автомобилю
    List<Auction> findByVehicleId(Long vehicleId);

    // Поиск последнего аукциона по автомобилю (для детальной информации)
    Optional<Auction> findFirstByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    // Проверка существования аукциона с конкретным статусом
    boolean existsByVehicleIdAndStatus(Long vehicleId, AuctionStatus status);

    // ОПТИМИЗИРОВАННЫЙ МЕТОД: проверка наличия любого из запрещённых статусов
    boolean existsByVehicleIdAndStatusIn(Long vehicleId, List<AuctionStatus> statuses);

    // ПОЛЕЗНЫЙ МЕТОД: получение всех статусов автомобиля одним запросом
    List<AuctionStatus> findStatusesByVehicleId(Long vehicleId);

    // НОВЫЙ МЕТОД: поиск с пессимистичной блокировкой для конкурентного доступа
    Optional<Auction> findByIdWithLock(Long id);
}