package com.example.autoauction.auction.domain;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@Slf4j
public class Auction {
    private Long id;
    private Long vehicleId;
    private String vehicleInfo;

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

    // Для optimistic locking
    private Long version;

    public Auction(Long vehicleId, BigDecimal startingPrice, BigDecimal reservePrice,
                   BigDecimal buyNowPrice, BigDecimal minBidStep,
                   OffsetDateTime startTime, OffsetDateTime endTime, Long createdBy) {

        Objects.requireNonNull(vehicleId, "ID автомобиля не может быть null");
        Objects.requireNonNull(startingPrice, "Стартовая цена не может быть null");
        Objects.requireNonNull(minBidStep, "Шаг ставки не может быть null");
        Objects.requireNonNull(startTime, "Время начала не может быть null");
        Objects.requireNonNull(endTime, "Время окончания не может быть null");
        Objects.requireNonNull(createdBy, "ID создателя не может быть null");

        validatePrices(startingPrice, reservePrice, buyNowPrice);
        validateTime(startTime, endTime);

        this.vehicleId = vehicleId;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
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
        this.version = 0L;
    }

    private void validatePrices(BigDecimal startingPrice, BigDecimal reservePrice, BigDecimal buyNowPrice) {
        if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Стартовая цена должна быть больше 0");
        }

        if (reservePrice != null) {
            if (reservePrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Резервная цена должна быть больше 0");
            }
            if (reservePrice.compareTo(startingPrice) < 0) {
                throw new IllegalArgumentException(
                        String.format("Резервная цена (%s) не может быть меньше стартовой (%s)",
                                reservePrice, startingPrice)
                );
            }
        }

        if (buyNowPrice != null) {
            if (buyNowPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Цена мгновенной покупки должна быть больше 0");
            }
            if (buyNowPrice.compareTo(startingPrice) <= 0) {
                throw new IllegalArgumentException(
                        String.format("Цена мгновенной покупки (%s) должна быть больше стартовой (%s)",
                                buyNowPrice, startingPrice)
                );
            }
        }
    }

    private void validateTime(OffsetDateTime startTime, OffsetDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    String.format("Время окончания (%s) должно быть позже времени начала (%s)",
                            endTime, startTime)
            );
        }
    }

    // Геттеры
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
    public Long getVersion() { return version; }

    // Сеттеры с валидацией
    public void setId(Long id) {
        Objects.requireNonNull(id, "ID не может быть null");
        this.id = id;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        Objects.requireNonNull(currentPrice, "Текущая цена не может быть null");
        if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Текущая цена должна быть больше 0");
        }
        this.currentPrice = currentPrice;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setStatus(AuctionStatus status) {
        Objects.requireNonNull(status, "Статус не может быть null");
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setWinningBid(BigDecimal winningBid) {
        if (winningBid != null && winningBid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Выигрышная ставка должна быть больше 0");
        }
        this.winningBid = winningBid;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setTotalBids(Integer totalBids) {
        Objects.requireNonNull(totalBids, "Количество ставок не может быть null");
        if (totalBids < 0) {
            throw new IllegalArgumentException("Количество ставок не может быть отрицательным");
        }
        this.totalBids = totalBids;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        Objects.requireNonNull(createdAt, "Дата создания не может быть null");
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        Objects.requireNonNull(updatedAt, "Дата обновления не может быть null");
        this.updatedAt = updatedAt;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Бизнес-методы с улучшенной валидацией
    public void updateVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
        this.updatedAt = OffsetDateTime.now();
    }

    public void start() {
        if (this.status != AuctionStatus.CREATED) {
            throw new IllegalStateException(
                    String.format("Невозможно запустить аукцион в статусе '%s'. Только аукционы со статусом 'CREATED' могут быть запущены",
                            this.status)
            );
        }
        this.status = AuctionStatus.ACTIVE;
        this.updatedAt = OffsetDateTime.now();
    }

    public void cancel() {
        if (this.status != AuctionStatus.CREATED && this.status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException(
                    String.format("Невозможно отменить аукцион в статусе '%s'. Только аукционы со статусами 'CREATED' или 'ACTIVE' могут быть отменены",
                            this.status)
            );
        }
        this.status = AuctionStatus.CANCELLED;
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Добавляет ставку на аукцион
     * @param amount Сумма ставки
     * @param bidderId ID участника
     * @param bidTime Время ставки (для проверки продления)
     */
    public void addBid(BigDecimal amount, Long bidderId, OffsetDateTime bidTime) {
        Objects.requireNonNull(amount, "Сумма ставки не может быть null");
        Objects.requireNonNull(bidderId, "ID участника не может быть null");
        Objects.requireNonNull(bidTime, "Время ставки не может быть null");

        if (this.status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException(
                    String.format("Невозможно сделать ставку на аукцион в статусе '%s'. Только активные аукционы принимают ставки",
                            this.status)
            );
        }

        // Проверяем, не истекло ли время (с учетом возможного продления)
        if (this.endTime.isBefore(bidTime)) {
            throw new IllegalStateException(
                    String.format("Время аукциона истекло. Время окончания: %s", this.endTime)
            );
        }

        BigDecimal minimumNextBid = this.currentPrice.add(this.minBidStep);
        if (amount.compareTo(minimumNextBid) < 0) {
            throw new IllegalArgumentException(
                    String.format("Сумма ставки %s слишком мала. Минимальная следующая ставка: %s",
                            amount, minimumNextBid)
            );
        }

        if (bidderId.equals(this.createdBy)) {
            throw new IllegalArgumentException("Нельзя делать ставки на собственный аукцион");
        }

        // Проверяем, нужно ли продлить аукцион (если ставка сделана в последние минуты)
        boolean extended = false;
        if (shouldExtendAuction(bidTime)) {
            extendAuction();
            extended = true;
        }

        // Обработка ставки
        if (this.buyNowPrice != null && amount.compareTo(this.buyNowPrice) >= 0) {
            this.currentPrice = this.buyNowPrice;
            this.winnerId = bidderId;
            this.winningBid = this.buyNowPrice;
            this.status = AuctionStatus.COMPLETED;
        } else {
            this.currentPrice = amount;
            this.winnerId = bidderId;
            this.winningBid = amount;
        }

        this.totalBids++;
        this.updatedAt = OffsetDateTime.now();

        if (extended) {
            log.debug("Auction {} extended due to late bid. New end time: {}", this.id, this.endTime);
        }
    }

    /**
     * Проверяет, нужно ли продлить аукцион
     * @param bidTime Время ставки
     * @return true если нужно продлить
     */
    private boolean shouldExtendAuction(OffsetDateTime bidTime) {
        // Параметры продления (можно вынести в конфигурацию)
        int EXTENSION_MINUTES = 5;      // На сколько минут продлевать
        int LAST_MINUTES_THRESHOLD = 5; // За сколько минут до конца продлевать

        long minutesUntilEnd = java.time.Duration.between(bidTime, this.endTime).toMinutes();
        return minutesUntilEnd <= LAST_MINUTES_THRESHOLD && minutesUntilEnd > 0;
    }

    /**
     * Продлевает аукцион
     */
    private void extendAuction() {
        int EXTENSION_MINUTES = 5; // На сколько минут продлевать
        this.endTime = this.endTime.plusMinutes(EXTENSION_MINUTES);
        this.updatedAt = OffsetDateTime.now();
    }

    // Перегрузка метода для обратной совместимости
    public void addBid(BigDecimal amount, Long bidderId) {
        addBid(amount, bidderId, OffsetDateTime.now());
    }

    // Вспомогательные методы
    public boolean isActive() {
        return this.status == AuctionStatus.ACTIVE;
    }

    public boolean isFinished() {
        return this.status == AuctionStatus.COMPLETED ||
                this.status == AuctionStatus.SOLD ||
                this.status == AuctionStatus.EXPIRED ||
                this.status == AuctionStatus.CANCELLED;
    }

    public boolean canBeStarted() {
        return this.status == AuctionStatus.CREATED &&
                this.startTime.isAfter(OffsetDateTime.now());
    }

    @Override
    public String toString() {
        return String.format("Auction{id=%d, vehicleId=%d, status=%s, currentPrice=%s, version=%d}",
                id, vehicleId, status, currentPrice, version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auction auction = (Auction) o;
        return Objects.equals(id, auction.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}