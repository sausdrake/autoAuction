package com.example.autoauction.deposit.domain.port;

import com.example.autoauction.deposit.domain.Deposit;
import com.example.autoauction.deposit.domain.DepositTransaction;

import java.util.List;
import java.util.Optional;

public interface DepositRepository {
    // Депозиты
    Deposit save(Deposit deposit);
    Optional<Deposit> findById(Long id);
    Optional<Deposit> findByUserId(Long userId);
    List<Deposit> findAll();
    void deleteById(Long id);

    // Транзакции
    DepositTransaction saveTransaction(DepositTransaction transaction);
    List<DepositTransaction> findTransactionsByUserId(Long userId);
    List<DepositTransaction> findTransactionsByDepositId(Long depositId);
    List<DepositTransaction> findTransactionsByAuctionId(Long auctionId);
}