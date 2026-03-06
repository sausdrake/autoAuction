package com.example.autoauction.vehicle.domain;

public enum VehicleStatus {
    DRAFT,              // Черновик (диагностик создает)
    PENDING_REVIEW,     // На проверке у админа
    NEEDS_FIXES,        // Требует доработки
    APPROVED,           // Одобрен, ждет аукциона
    IN_AUCTION,         // В аукционе
    SOLD,               // Продан
    WITHDRAWN           // Снят
}