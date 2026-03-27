package com.example.autoauction.auth.infrastructure.persistence;

import com.example.autoauction.auth.domain.RefreshToken;
import com.example.autoauction.auth.domain.port.RefreshTokenRepository;
import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.infrastructure.persistence.JpaUserEntity;
import com.example.autoauction.user.infrastructure.persistence.SpringDataUserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Transactional
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenJpaRepository jpaRepository;
    private final SpringDataUserJpaRepository userJpaRepository;
    private final RefreshTokenMapper mapper;

    public JpaRefreshTokenRepositoryAdapter(
            SpringDataRefreshTokenJpaRepository jpaRepository,
            SpringDataUserJpaRepository userJpaRepository,
            RefreshTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        JpaUserEntity userEntity = userJpaRepository.findById(refreshToken.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + refreshToken.getUser().getId()));

        JpaRefreshTokenEntity entity = mapper.toEntity(refreshToken, userEntity);
        JpaRefreshTokenEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findByUser(User user) {
        return jpaRepository.findByUserId(user.getId()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findValidTokensByUser(User user) {
        return jpaRepository.findByUserIdAndRevokedFalse(user.getId()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void revokeAllUserTokens(User user) {
        jpaRepository.revokeAllUserTokens(user.getId());
        log.info("Revoked all refresh tokens for user: {}", user.getUsername());
    }

    @Override
    public void deleteExpiredTokens() {
        jpaRepository.deleteAllExpiredTokens(Instant.now());
        log.debug("Deleted expired refresh tokens");
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByToken(String token) {
        return jpaRepository.findByToken(token).isPresent();
    }
}