package com.example.autoauction.auction.infrastructure.scheduler;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.AuctionStatus;
import com.example.autoauction.auction.domain.port.AuctionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
public class AuctionCompletionScheduler {

    private final AuctionRepository auctionRepository;

    public AuctionCompletionScheduler(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    /**
     * Завершает аукционы у которых истекло время окончания
     * Запускается каждую минуту
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void completeExpiredAuctions() {
        log.debug("Checking for expired auctions...");

        List<Auction> activeAuctions = auctionRepository.findByStatus(AuctionStatus.ACTIVE);

        int completedCount = 0;

        for (Auction auction : activeAuctions) {
            // Проверяем, истекло ли время окончания
            if (auction.getEndTime().isBefore(OffsetDateTime.now())) {
                log.info("Auction {} (vehicle {}) has ended. End time: {}, Current time: {}",
                        auction.getId(), auction.getVehicleId(),
                        auction.getEndTime(), OffsetDateTime.now());

                finalizeAuction(auction);
                completedCount++;
            }
        }

        if (completedCount > 0) {
            log.info("Finalized {} expired auctions", completedCount);
        }
    }

    /**
     * Финализирует аукцион после окончания времени
     */
    private void finalizeAuction(Auction auction) {
        try {
            // Проверяем, были ли ставки
            if (auction.getTotalBids() == null || auction.getTotalBids() == 0) {
                // Нет ставок
                auction.setStatus(AuctionStatus.EXPIRED);
                auction.setWinnerId(null);
                auction.setWinningBid(null);
                log.info("Auction {} expired with no bids", auction.getId());

            } else {
                // Есть ставки - проверяем резервную цену
                boolean reserveMet = auction.getReservePrice() == null ||
                        auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0;

                if (reserveMet) {
                    // Резервная цена достигнута - победитель текущий лидер
                    auction.setStatus(AuctionStatus.COMPLETED);
                    log.info("Auction {} completed successfully. Winner: {}, Winning bid: {}, Reserve price: {}",
                            auction.getId(), auction.getWinnerId(),
                            auction.getWinningBid(), auction.getReservePrice());
                } else {
                    // Резервная цена не достигнута - аукцион не состоялся
                    auction.setStatus(AuctionStatus.EXPIRED);
                    auction.setWinnerId(null);
                    auction.setWinningBid(null);
                    log.info("Auction {} expired - reserve price not met. Final price: {}, Reserve price: {}",
                            auction.getId(), auction.getCurrentPrice(), auction.getReservePrice());
                }
            }

            auction.setUpdatedAt(OffsetDateTime.now());
            auctionRepository.save(auction);

        } catch (Exception e) {
            log.error("Error finalizing auction {}: {}", auction.getId(), e.getMessage(), e);
        }
    }

    /**
     * Отменяет аукционы со статусом CREATED, которые не были запущены вовремя
     * Запускается каждый час
     */
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cancelExpiredCreatedAuctions() {
        log.debug("Checking for expired CREATED auctions...");

        List<Auction> createdAuctions = auctionRepository.findByStatus(AuctionStatus.CREATED);

        int cancelledCount = 0;

        for (Auction auction : createdAuctions) {
            // Если время начала уже прошло, а аукцион не был запущен
            if (auction.getStartTime().isBefore(OffsetDateTime.now())) {
                log.info("Cancelling auction {} - start time {} has passed but auction was not started",
                        auction.getId(), auction.getStartTime());

                auction.setStatus(AuctionStatus.CANCELLED);
                auction.setUpdatedAt(OffsetDateTime.now());
                auctionRepository.save(auction);
                cancelledCount++;
            }
        }

        if (cancelledCount > 0) {
            log.info("Cancelled {} expired CREATED auctions", cancelledCount);
        }
    }
}