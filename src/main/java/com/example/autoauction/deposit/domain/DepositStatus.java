package com.example.autoauction.deposit.domain;

public enum DepositStatus {
    ACTIVE,     // Активный депозит
    FROZEN,     // Заморожен (из-за нарушений)
    CLOSED      // Закрыт
}