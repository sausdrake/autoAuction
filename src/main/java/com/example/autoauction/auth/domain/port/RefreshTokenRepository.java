package com.example.autoauction.auth.domain.port;

import com.example.autoauction.auth.domain.RefreshToken;
import com.example.autoauction.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    List<RefreshToken> findValidTokensByUser(User user);

    void revokeAllUserTokens(User user);

    void deleteExpiredTokens();

    void deleteById(Long id);

    boolean existsByToken(String token);
}