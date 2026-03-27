package com.example.autoauction.user.infrastructure.persistence;

import com.example.autoauction.user.domain.Role;
import com.example.autoauction.user.domain.User;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class UserMapper {  // ← Добавляем public и final

    private UserMapper() {  // Приватный конструктор для utility class
    }

    public static User toDomain(JpaUserEntity entity) {  // ← public
        if (entity == null) return null;

        Set<Role> roles = entity.getRoles().stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.toSet());

        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.isActive(),
                roles
        );
    }

    public static Role toDomain(JpaRoleEntity entity) {  // ← public
        if (entity == null) return null;

        return new Role(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }

    // Добавим метод для конвертации из домена в JPA (может пригодиться)
    public static JpaUserEntity toEntity(User domain) {
        if (domain == null) return null;

        JpaUserEntity entity = new JpaUserEntity(
                domain.getUsername(),
                domain.getEmail(),
                domain.getPasswordHash(),
                domain.isActive()
        );
        entity.setId(domain.getId());

        // Конвертируем роли
        Set<JpaRoleEntity> roleEntities = domain.getRoles().stream()
                .map(UserMapper::toEntity)
                .collect(Collectors.toSet());
        entity.setRoles(roleEntities);

        return entity;
    }

    public static JpaRoleEntity toEntity(Role domain) {
        if (domain == null) return null;

        return new JpaRoleEntity(
                domain.getName(),
                domain.getDescription()
        );
    }
}