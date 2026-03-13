package com.example.autoauction.user.infrastructure.persistence;

import com.example.autoauction.user.domain.Role;
import com.example.autoauction.user.domain.User;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
final class UserMapper {

    private UserMapper() {
    }

    static User toDomain(JpaUserEntity entity) {
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

    static Role toDomain(JpaRoleEntity entity) {
        return new Role(entity.getId(), entity.getName(), entity.getDescription());
    }
}

