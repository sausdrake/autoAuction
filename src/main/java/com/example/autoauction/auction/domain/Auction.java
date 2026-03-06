package com.example.autoauction.auction.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Auction {
    private Long id;
    private Long vehicleId;
    private String vehicleInfo;      // Кэшированная информация об авто

    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal reservePrice;
    private BigDecimal buyNowPrice;
    private BigDecimal minBidStep;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    private AuctionStatus status;
    private Long winnerId;
    private BigDecimal winningBid;
    private Integer totalBids;

    private Long createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Конструктор
    public Auction(Long vehicleId, BigDecimal startingPrice, BigDecimal reservePrice,
                   BigDecimal buyNowPrice, BigDecimal minBidStep,
                   OffsetDateTime startTime, OffsetDateTime endTime, Long createdBy) {
        this.vehicleId = vehicleId;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;  // Изначально равна стартовой
        this.reservePrice = reservePrice;
        this.buyNowPrice = buyNowPrice;
        this.minBidStep = minBidStep;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdBy = createdBy;
        this.status = AuctionStatus.CREATED;
        this.totalBids = 0;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }


    // Геттеры (без сеттеров для неизменяемости)
    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public String getVehicleInfo() { return vehicleInfo; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getReservePrice() { return reservePrice; }
    public BigDecimal getBuyNowPrice() { return buyNowPrice; }
    public BigDecimal getMinBidStep() { return minBidStep; }
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public AuctionStatus getStatus() { return status; }
    public Long getWinnerId() { return winnerId; }
    public BigDecimal getWinningBid() { return winningBid; }
    public Integer getTotalBids() { return totalBids; }
    public Long getCreatedBy() { return createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // Бизнес-методы
    public void updateVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
        this.updatedAt = OffsetDateTime.now();
    }

    public void start() {
        if (this.status != AuctionStatus.CREATED) {
            throw new IllegalStateException("Only CREATED auction can be started");
        }
        this.status = AuctionStatus.ACTIVE;
        this.updatedAt = OffsetDateTime.now();
    }

    public void cancel() {
        if (this.status != AuctionStatus.CREATED && this.status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot cancel auction in status: " + this.status);
        }
        this.status = AuctionStatus.CANCELLED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void addBid(BigDecimal amount, Long bidderId) {
        if (this.status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active");
        }
        if (amount.compareTo(this.currentPrice.add(this.minBidStep)) < 0) {
            throw new IllegalArgumentException("Bid amount is too low");
        }
        if (bidderId.equals(this.createdBy)) {
            throw new IllegalArgumentException("Cannot bid on your own auction");
        }

        this.currentPrice = amount;
        this.totalBids++;
        this.updatedAt = OffsetDateTime.now();
    }
    // Добавь эти сеттеры в класс Auction

    public void setId(Long id) {
        this.id = id;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public void setWinningBid(BigDecimal winningBid) {
        this.winningBid = winningBid;
    }

    public void setTotalBids(Integer totalBids) {
        this.totalBids = totalBids;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}