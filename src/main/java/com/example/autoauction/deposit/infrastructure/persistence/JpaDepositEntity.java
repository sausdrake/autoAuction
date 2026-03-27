package com.example.autoauction.deposit.infrastructure.persistence;

import com.example.autoauction.deposit.domain.DepositStatus;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Entity
@Table(name = "deposits")
public class JpaDepositEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "blocked_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal blockedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DepositStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    protected JpaDepositEntity() {}

    public JpaDepositEntity(Long userId, BigDecimal balance, BigDecimal blockedAmount, DepositStatus status) {
        this.userId = userId;
        this.balance = balance;
        this.blockedAmount = blockedAmount;
        this.status = status;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getBlockedAmount() { return blockedAmount; }
    public void setBlockedAmount(BigDecimal blockedAmount) { this.blockedAmount = blockedAmount; }

    public DepositStatus getStatus() { return status; }
    public void setStatus(DepositStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}