package com.example.autoauction.auth.infrastructure.persistence;

import com.example.autoauction.user.infrastructure.persistence.JpaUserEntity;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@Entity
@Table(name = "refresh_tokens")
public class JpaRefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private JpaUserEntity user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Конструктор по умолчанию для JPA
    protected JpaRefreshTokenEntity() {}

    public JpaRefreshTokenEntity(String token, JpaUserEntity user, Instant expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.createdAt = Instant.now();
        this.revoked = false;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public JpaUserEntity getUser() { return user; }
    public void setUser(JpaUserEntity user) { this.user = user; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}