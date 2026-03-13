package com.example.autoauction.auction.infrastructure.persistence;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.AuctionStatus;
import com.example.autoauction.auction.domain.port.AuctionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Repository
@Transactional
public class JpaAuctionRepositoryAdapter implements AuctionRepository {

    private final SpringDataAuctionJpaRepository jpaRepository;
    private final AuctionMapper mapper;

    public JpaAuctionRepositoryAdapter(
            SpringDataAuctionJpaRepository jpaRepository,
            AuctionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Auction save(Auction auction) {
        JpaAuctionEntity entity = mapper.toEntity(auction);
        JpaAuctionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Auction> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> findByStatus(AuctionStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> findByVehicleId(Long vehicleId) {
        return jpaRepository.findByVehicleId(vehicleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByVehicleIdAndStatus(Long vehicleId, AuctionStatus status) {
        return jpaRepository.existsByVehicleIdAndStatus(vehicleId, status);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}