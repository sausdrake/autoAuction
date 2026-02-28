package com.example.autoauction.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRoleJpaRepository extends JpaRepository<JpaRoleEntity, Long> {

    Optional<JpaRoleEntity> findByName(String name);
}

