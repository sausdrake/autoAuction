package com.example.autoauction.auction.infrastructure.scheduler;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.AuctionStatus;
import com.example.autoauction.auction.domain.port.AuctionRepository;
import com.example.autoauction.deposit.application.DepositService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@EnableScheduling
public class AuctionCompletionScheduler {

    private final AuctionRepository auctionRepository;
    private final DepositService depositService;

    // Кэш для отслеживания участников аукционов
    private final ConcurrentHashMap<Long, Set<Long>> auctionParticipants = new ConcurrentHashMap<>();

    public AuctionCompletionScheduler(AuctionRepository auctionRepository,
                                      DepositService depositService) {
        this.auctionRepository = auctionRepository;
        this.depositService = depositService;
        log.info("AuctionCompletionScheduler initialized with deposit service integration");
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
            AuctionStatus finalStatus;
            Long winnerId = null;
            BigDecimal winningBid = null;
            Set<Long> participants = auctionParticipants.getOrDefault(auction.getId(), Set.of());

            // Проверяем, были ли ставки
            if (auction.getTotalBids() == null || auction.getTotalBids() == 0) {
                // Нет ставок
                finalStatus = AuctionStatus.EXPIRED;
                log.info("Auction {} expired with no bids", auction.getId());

                // Размораживаем депозиты всех участников
                for (Long participantId : participants) {
                    try {
                        depositService.unfreezeForAuction(participantId, auction.getId(), auction.getStartingPrice());
                        log.info("Deposit unfrozen for user {} on auction {} (no bids)",
                                participantId, auction.getId());
                    } catch (Exception e) {
                        log.warn("Failed to unfreeze deposit for user {} on auction {}: {}",
                                participantId, auction.getId(), e.getMessage());
                    }
                }

            } else {
                // Есть ставки - проверяем резервную цену
                boolean reserveMet = auction.getReservePrice() == null ||
                        auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0;

                if (reserveMet) {
                    // Резервная цена достигнута - победитель текущий лидер
                    finalStatus = AuctionStatus.COMPLETED;
                    winnerId = auction.getWinnerId();
                    winningBid = auction.getWinningBid();

                    log.info("Auction {} completed successfully. Winner: {}, Winning bid: {}, Reserve price: {}",
                            auction.getId(), winnerId, winningBid, auction.getReservePrice());

                    // Отмечаем победителя в депозите (депозит остается замороженным)
                    if (winnerId != null) {
                        try {
                            depositService.markAsWinner(winnerId, auction.getId());
                            log.info("User {} marked as winner of auction {}", winnerId, auction.getId());
                        } catch (Exception e) {
                            log.error("Failed to mark winner {} for auction {}: {}",
                                    winnerId, auction.getId(), e.getMessage());
                        }
                    }

                    // Размораживаем депозиты всех проигравших участников
                    for (Long participantId : participants) {
                        if (!participantId.equals(winnerId)) {
                            try {
                                depositService.unfreezeForAuction(participantId, auction.getId(), auction.getStartingPrice());
                                log.info("Deposit unfrozen for loser user {} on auction {}",
                                        participantId, auction.getId());
                            } catch (Exception e) {
                                log.warn("Failed to unfreeze deposit for user {} on auction {}: {}",
                                        participantId, auction.getId(), e.getMessage());
                            }
                        }
                    }

                } else {
                    // Резервная цена не достигнута - аукцион не состоялся
                    finalStatus = AuctionStatus.EXPIRED;
                    log.info("Auction {} expired - reserve price not met. Final price: {}, Reserve price: {}",
                            auction.getId(), auction.getCurrentPrice(), auction.getReservePrice());

                    // Размораживаем депозиты всех участников (никто не победил)
                    for (Long participantId : participants) {
                        try {
                            depositService.unfreezeForAuction(participantId, auction.getId(), auction.getStartingPrice());
                            log.info("Deposit unfrozen for user {} on auction {} (reserve not met)",
                                    participantId, auction.getId());
                        } catch (Exception e) {
                            log.warn("Failed to unfreeze deposit for user {} on auction {}: {}",
                                    participantId, auction.getId(), e.getMessage());
                        }
                    }
                }
            }

            // Обновляем аукцион
            auction.setStatus(finalStatus);
            auction.setWinnerId(winnerId);
            auction.setWinningBid(winningBid);
            auction.setUpdatedAt(OffsetDateTime.now());
            auctionRepository.save(auction);

            // Очищаем кэш участников для этого аукциона
            auctionParticipants.remove(auction.getId());

            log.info("Auction {} finalized with status: {}", auction.getId(), finalStatus);

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

                // Очищаем кэш участников если есть
                auctionParticipants.remove(auction.getId());
                cancelledCount++;
            }
        }

        if (cancelledCount > 0) {
            log.info("Cancelled {} expired CREATED auctions", cancelledCount);
        }
    }

    /**
     * Регистрирует участника аукциона (вызывается при первой ставке)
     */
    public void registerParticipant(Long auctionId, Long userId) {
        auctionParticipants.computeIfAbsent(auctionId, k -> ConcurrentHashMap.newKeySet())
                .add(userId);
        log.debug("User {} registered as participant of auction {}", userId, auctionId);
    }

    /**
     * Проверяет, участвует ли пользователь в аукционе
     */
    public boolean isParticipant(Long auctionId, Long userId) {
        Set<Long> participants = auctionParticipants.get(auctionId);
        return participants != null && participants.contains(userId);
    }

    /**
     * Получить всех участников аукциона
     */
    public Set<Long> getParticipants(Long auctionId) {
        return auctionParticipants.getOrDefault(auctionId, Set.of());
    }

    /**
     * Очистить участников аукциона (вызывается при принудительном завершении)
     */
    public void clearParticipants(Long auctionId) {
        auctionParticipants.remove(auctionId);
        log.debug("Cleared participants for auction {}", auctionId);
    }
}