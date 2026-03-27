package com.example.autoauction.deposit.infrastructure.persistence;

import com.example.autoauction.deposit.domain.Deposit;
import com.example.autoauction.deposit.domain.DepositTransaction;
import com.example.autoauction.deposit.domain.port.DepositRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Transactional
public class JpaDepositRepositoryAdapter implements DepositRepository {

    private final SpringDataDepositJpaRepository depositJpaRepository;
    private final SpringDataDepositTransactionJpaRepository transactionJpaRepository;
    private final DepositMapper mapper;

    public JpaDepositRepositoryAdapter(SpringDataDepositJpaRepository depositJpaRepository,
                                       SpringDataDepositTransactionJpaRepository transactionJpaRepository,
                                       DepositMapper mapper) {
        this.depositJpaRepository = depositJpaRepository;
        this.transactionJpaRepository = transactionJpaRepository;
        this.mapper = mapper;
        log.info("JpaDepositRepositoryAdapter initialized");
    }

    @Override
    public Deposit save(Deposit deposit) {
        JpaDepositEntity entity = mapper.toEntity(deposit);
        JpaDepositEntity saved = depositJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Deposit> findById(Long id) {
        return depositJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Deposit> findByUserId(Long userId) {
        return depositJpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deposit> findAll() {
        return depositJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        depositJpaRepository.deleteById(id);
    }

    @Override
    public DepositTransaction saveTransaction(DepositTransaction transaction) {
        JpaDepositTransactionEntity entity = mapper.toEntity(transaction);
        JpaDepositTransactionEntity saved = transactionJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositTransaction> findTransactionsByUserId(Long userId) {
        return transactionJpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositTransaction> findTransactionsByDepositId(Long depositId) {
        return transactionJpaRepository.findByDepositId(depositId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositTransaction> findTransactionsByAuctionId(Long auctionId) {
        return transactionJpaRepository.findByAuctionId(auctionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}