package com.example.autoauction.auction.infrastructure.persistence;

import com.example.autoauction.auction.domain.AuctionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataAuctionJpaRepository extends JpaRepository<JpaAuctionEntity, Long> {

    List<JpaAuctionEntity> findByStatus(AuctionStatus status);

    List<JpaAuctionEntity> findByVehicleId(Long vehicleId);

    Optional<JpaAuctionEntity> findFirstByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    boolean existsByVehicleIdAndStatus(Long vehicleId, AuctionStatus status);

    boolean existsByVehicleIdAndStatusIn(Long vehicleId, List<AuctionStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM JpaAuctionEntity a WHERE a.id = :id")
    Optional<JpaAuctionEntity> findByIdWithLock(@Param("id") Long id);
}