package com.example.autoauction.auth.domain;

import com.example.autoauction.user.domain.User;
import java.time.Instant;

public class RefreshToken {
    private final Long id;
    private final String token;
    private final User user;
    private final Instant expiryDate;
    private final boolean revoked;

    public RefreshToken(Long id, String token, User user, Instant expiryDate, boolean revoked) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
    }

    // Геттеры
    public Long getId() { return id; }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public Instant getExpiryDate() { return expiryDate; }
    public boolean isRevoked() { return revoked; }
}