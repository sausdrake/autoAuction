package com.example.autoauction.auction.domain;

public enum AuctionStatus {
    CREATED,      // Создан, ещё не начался
    ACTIVE,       // Торги идут
    COMPLETED,    // Завершён (есть победитель)
    SOLD,         // Автомобиль продан
    EXPIRED,      // Истёк без ставок
    CANCELLED     // Отменён администратором
}