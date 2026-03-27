package com.example.autoauction.auction.web.dto;

import com.example.autoauction.auction.domain.AuctionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Ответ после создания ставки")
public record BidResponse(

        @Schema(description = "ID аукциона", example = "1")
        Long auctionId,

        @Schema(description = "Сумма ставки", example = "1050000")
        BigDecimal amount,

        @Schema(description = "Новая текущая цена", example = "1050000")
        BigDecimal newCurrentPrice,

        @Schema(description = "Количество ставок", example = "5")
        Integer totalBids,

        @Schema(description = "Статус аукциона после ставки")
        AuctionStatus status,

        @Schema(description = "Была ли использована цена мгновенной покупки", example = "false")
        boolean buyNowUsed,

        @Schema(description = "Был ли продлен аукцион", example = "false")
        boolean auctionExtended,

        @Schema(description = "Новое время окончания (если было продление)")
        OffsetDateTime newEndTime,

        @Schema(description = "Время ставки")
        OffsetDateTime timestamp
) {
    public static BidResponse fromDomain(
            com.example.autoauction.auction.domain.Auction auction,
            BigDecimal amount,
            boolean buyNowUsed,
            boolean auctionExtended
    ) {
        return new BidResponse(
                auction.getId(),
                amount,
                auction.getCurrentPrice(),
                auction.getTotalBids(),
                auction.getStatus(),
                buyNowUsed,
                auctionExtended,
                auction.getEndTime(),
                OffsetDateTime.now()
        );
    }
}