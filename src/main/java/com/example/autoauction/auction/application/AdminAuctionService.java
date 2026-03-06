package com.example.autoauction.auction.application;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.AuctionStatus;
import com.example.autoauction.auction.domain.port.AuctionRepository;
import com.example.autoauction.auction.web.dto.AuctionCreateRequest;
import com.example.autoauction.auction.web.dto.AuctionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminAuctionService {

    private final AuctionRepository auctionRepository;

    public AdminAuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Transactional
    public AuctionResponse createAuction(AuctionCreateRequest request, Long adminId) {
        // Проверяем, нет ли уже активного аукциона для этого авто
        if (auctionRepository.existsByVehicleIdAndStatus(request.vehicleId(), AuctionStatus.ACTIVE)) {
            throw new IllegalArgumentException("Автомобиль уже участвует в активном аукционе");
        }

        // Создаём аукцион
        Auction auction = new Auction(
                request.vehicleId(),
                request.startingPrice(),
                request.reservePrice(),
                request.buyNowPrice(),
                request.minBidStep(),
                request.startTime(),
                request.endTime(),
                adminId
        );

        // TODO: Получить информацию об авто из Vehicle модуля
        // auction.setVehicleInfo(...);

        Auction saved = auctionRepository.save(auction);
        return AuctionResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(AuctionResponse::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuctionResponse getAuction(Long id) {
        return auctionRepository.findById(id)
                .map(AuctionResponse::fromDomain)
                .orElseThrow(() -> new IllegalArgumentException("Аукцион не найден: " + id));
    }

    @Transactional
    public AuctionResponse startAuction(Long id, Long adminId) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аукцион не найден: " + id));

        auction.start();
        Auction saved = auctionRepository.save(auction);
        return AuctionResponse.fromDomain(saved);
    }

    @Transactional
    public AuctionResponse cancelAuction(Long id, Long adminId) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аукцион не найден: " + id));

        auction.cancel();
        Auction saved = auctionRepository.save(auction);
        return AuctionResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAuctionsByStatus(AuctionStatus status) {
        return auctionRepository.findByStatus(status).stream()
                .map(AuctionResponse::fromDomain)
                .collect(Collectors.toList());
    }
}