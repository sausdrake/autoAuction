package com.example.autoauction.auction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Запрос на создание ставки")
public record BidRequest(

        @Schema(description = "Сумма ставки", example = "1050000", required = true)
        @NotNull(message = "Сумма ставки обязательна")
        @DecimalMin(value = "0.01", message = "Сумма ставки должна быть больше 0")
        BigDecimal amount
) {}