package com.example.autoauction.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataRefreshTokenJpaRepository extends JpaRepository<JpaRefreshTokenEntity, Long> {

    Optional<JpaRefreshTokenEntity> findByToken(String token);

    List<JpaRefreshTokenEntity> findByUserId(Long userId);

    List<JpaRefreshTokenEntity> findByUserIdAndRevokedFalse(Long userId);

    @Modifying
    @Query("UPDATE JpaRefreshTokenEntity rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllUserTokens(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM JpaRefreshTokenEntity rt WHERE rt.expiryDate < :now")
    void deleteAllExpiredTokens(@Param("now") Instant now);

    List<JpaRefreshTokenEntity> findByRevokedFalseAndExpiryDateBefore(Instant now);
}