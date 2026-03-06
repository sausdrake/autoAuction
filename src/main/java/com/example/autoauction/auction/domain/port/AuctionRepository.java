package com.example.autoauction.auction.domain.port;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.AuctionStatus;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository {
    Auction save(Auction auction);
    Optional<Auction> findById(Long id);
    List<Auction> findAll();
    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findByVehicleId(Long vehicleId);
    boolean existsByVehicleIdAndStatus(Long vehicleId, AuctionStatus status);
    void deleteById(Long id);
}