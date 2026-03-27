package com.example.autoauction.deposit.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Запрос на операцию с депозитом")
public record DepositRequest(

        @Schema(description = "Сумма", example = "10000", required = true)
        @NotNull(message = "Сумма обязательна")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
        BigDecimal amount
) {}