package com.example.autoauction.auction.infrastructure.persistence;

import com.example.autoauction.auction.domain.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataAuctionJpaRepository extends JpaRepository<JpaAuctionEntity, Long> {

    List<JpaAuctionEntity> findByStatus(AuctionStatus status);

    Optional<JpaAuctionEntity> findByVehicleId(Long vehicleId);

    List<JpaAuctionEntity> findByVehicleIdAndStatus(Long vehicleId, AuctionStatus status);

    boolean existsByVehicleIdAndStatus(Long vehicleId, AuctionStatus status);
}