package com.example.autoauction.deposit.web.dto;

import com.example.autoauction.deposit.domain.DepositTransaction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Ответ с данными транзакции депозита")
public record DepositTransactionResponse(

        @Schema(description = "ID транзакции", example = "1")
        Long id,

        @Schema(description = "ID пользователя", example = "1")
        Long userId,

        @Schema(description = "ID аукциона", example = "1")
        Long auctionId,

        @Schema(description = "Тип транзакции")
        DepositTransaction.TransactionType type,

        @Schema(description = "Сумма", example = "10000")
        BigDecimal amount,

        @Schema(description = "Баланс до операции", example = "50000")
        BigDecimal balanceBefore,

        @Schema(description = "Баланс после операции", example = "60000")
        BigDecimal balanceAfter,

        @Schema(description = "Ссылка", example = "AUCTION_1")
        String reference,

        @Schema(description = "Описание")
        String description,

        @Schema(description = "Дата создания")
        OffsetDateTime createdAt
) {
    public static DepositTransactionResponse fromDomain(DepositTransaction transaction) {
        return new DepositTransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getAuctionId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getReference(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}