package com.example.autoauction.deposit.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataDepositJpaRepository extends JpaRepository<JpaDepositEntity, Long> {
    Optional<JpaDepositEntity> findByUserId(Long userId);
}