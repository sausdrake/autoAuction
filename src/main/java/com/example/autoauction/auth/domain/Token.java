package com.example.autoauction.auth.domain;

import com.example.autoauction.user.domain.User;
import java.time.Instant;

public class Token {
    private final Long id;
    private final String token;
    private final TokenType tokenType;
    private final boolean revoked;
    private final boolean expired;
    private final User user;
    private final Instant expiryDate;

    public Token(Long id, String token, TokenType tokenType, boolean revoked,
                 boolean expired, User user, Instant expiryDate) {
        this.id = id;
        this.token = token;
        this.tokenType = tokenType;
        this.revoked = revoked;
        this.expired = expired;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    public enum TokenType {
        BEARER
    }

    // Геттеры
    public Long getId() { return id; }
    public String getToken() { return token; }
    public TokenType getTokenType() { return tokenType; }
    public boolean isRevoked() { return revoked; }
    public boolean isExpired() { return expired; }
    public User getUser() { return user; }
    public Instant getExpiryDate() { return expiryDate; }
}