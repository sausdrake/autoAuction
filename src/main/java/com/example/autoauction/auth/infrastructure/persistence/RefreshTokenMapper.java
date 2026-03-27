// Обнови файл: auth/infrastructure/persistence/RefreshTokenMapper.java

package com.example.autoauction.auth.infrastructure.persistence;

import com.example.autoauction.auth.domain.RefreshToken;
import com.example.autoauction.user.infrastructure.persistence.JpaUserEntity;
import com.example.autoauction.user.infrastructure.persistence.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RefreshTokenMapper {

    public JpaRefreshTokenEntity toEntity(RefreshToken domain, JpaUserEntity userEntity) {
        if (domain == null) return null;

        JpaRefreshTokenEntity entity = new JpaRefreshTokenEntity(
                domain.getToken(),
                userEntity,
                domain.getExpiryDate()
        );
        entity.setId(domain.getId());
        entity.setRevoked(domain.isRevoked());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }

    public RefreshToken toDomain(JpaRefreshTokenEntity entity) {
        if (entity == null) return null;

        return new RefreshToken(
                entity.getId(),
                entity.getToken(),
                UserMapper.toDomain(entity.getUser()),
                entity.getExpiryDate(),
                entity.isRevoked(),
                entity.getCreatedAt()
        );
    }
}