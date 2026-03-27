package com.example.autoauction.deposit.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataDepositTransactionJpaRepository extends JpaRepository<JpaDepositTransactionEntity, Long> {
    List<JpaDepositTransactionEntity> findByUserId(Long userId);
    List<JpaDepositTransactionEntity> findByDepositId(Long depositId);
    List<JpaDepositTransactionEntity> findByAuctionId(Long auctionId);
}