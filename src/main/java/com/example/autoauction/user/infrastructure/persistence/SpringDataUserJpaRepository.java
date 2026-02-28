package com.example.autoauction.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserJpaRepository extends JpaRepository<JpaUserEntity, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<JpaUserEntity> findByUsername(String username);
}

