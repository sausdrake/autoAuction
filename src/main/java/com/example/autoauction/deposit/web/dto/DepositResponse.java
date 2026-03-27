// Файл: deposit/web/dto/DepositResponse.java

package com.example.autoauction.deposit.web.dto;

import com.example.autoauction.deposit.domain.Deposit;
import com.example.autoauction.deposit.domain.DepositStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Ответ с данными депозита")
public record DepositResponse(

        @Schema(description = "ID депозита", example = "1")
        Long id,

        @Schema(description = "ID пользователя", example = "1")
        Long userId,

        @Schema(description = "Текущий баланс", example = "50000")
        BigDecimal balance,

        @Schema(description = "Заблокированная сумма", example = "10000")
        BigDecimal blockedAmount,

        @Schema(description = "Доступный баланс", example = "40000")
        BigDecimal availableBalance,

        @Schema(description = "Статус депозита")
        DepositStatus status,

        @Schema(description = "Дата создания")
        OffsetDateTime createdAt
) {
    public static DepositResponse fromDomain(Deposit deposit) {
        return new DepositResponse(
                deposit.getId(),
                deposit.getUserId(),
                deposit.getBalance(),
                deposit.getBlockedAmount(),
                deposit.getAvailableBalance(),
                deposit.getStatus(),
                deposit.getCreatedAt()
        );
    }
}