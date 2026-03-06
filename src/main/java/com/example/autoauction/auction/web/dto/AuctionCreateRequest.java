package com.example.autoauction.auction.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Запрос на создание аукциона")
public record AuctionCreateRequest(

        @Schema(description = "ID автомобиля", example = "1")
        @NotNull(message = "ID автомобиля обязателен")
        Long vehicleId,

        @Schema(description = "Стартовая цена", example = "1000000")
        @NotNull(message = "Стартовая цена обязательна")
        @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
        BigDecimal startingPrice,

        @Schema(description = "Резервная цена (минимальная цена продажи)", example = "1200000")
        @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
        BigDecimal reservePrice,

        @Schema(description = "Цена мгновенной покупки", example = "1500000")
        @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
        BigDecimal buyNowPrice,

        @Schema(description = "Минимальный шаг ставки", example = "10000")
        @NotNull(message = "Шаг ставки обязателен")
        @DecimalMin(value = "0.01", message = "Шаг должен быть больше 0")
        BigDecimal minBidStep,

        @Schema(description = "Время начала аукциона", example = "2026-03-10T10:00:00+07:00")
        @NotNull(message = "Время начала обязательно")
        OffsetDateTime startTime,

        @Schema(description = "Время окончания аукциона", example = "2026-03-17T10:00:00+07:00")
        @NotNull(message = "Время окончания обязательно")
        OffsetDateTime endTime
) {}