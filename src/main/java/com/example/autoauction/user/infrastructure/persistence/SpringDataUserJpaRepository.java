package com.example.autoauction.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SpringDataUserJpaRepository extends JpaRepository<JpaUserEntity, Long> {

    Optional<JpaUserEntity> findByUsername(String username);

    Optional<JpaUserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}