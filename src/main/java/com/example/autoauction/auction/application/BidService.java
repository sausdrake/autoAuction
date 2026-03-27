// Файл: auction/application/BidService.java

package com.example.autoauction.auction.application;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.port.AuctionRepository;
import com.example.autoauction.auction.web.dto.BidResponse;
//import com.example.autoauction.deposit.domain.port.DepositRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Service
public class BidService {

    private final AuctionRepository auctionRepository;
    // TODO: Раскомментировать когда Deposit модуль будет готов
    // private final DepositRepository depositRepository;

    public BidService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    // Файл: auction/application/BidService.java

    @Transactional
    public BidResponse placeBid(Long auctionId, BigDecimal amount, Long bidderId) {
        log.info("=== СОЗДАНИЕ СТАВКИ ===");
        log.info("Auction ID: {}, Bidder ID: {}, Amount: {}", auctionId, bidderId, amount);

        // Загружаем аукцион с пессимистичной блокировкой
        Auction auction = auctionRepository.findByIdWithLock(auctionId)
                .orElseThrow(() -> {
                    log.error("Аукцион не найден: {}", auctionId);
                    return new IllegalArgumentException("Аукцион с ID " + auctionId + " не найден");
                });

        log.debug("Текущий статус аукциона: {}, текущая цена: {}, минимальный шаг: {}, время окончания: {}",
                auction.getStatus(), auction.getCurrentPrice(), auction.getMinBidStep(), auction.getEndTime());

        // Проверяем, не является ли участник создателем аукциона
        if (bidderId.equals(auction.getCreatedBy())) {
            log.warn("Попытка сделать ставку на свой аукцион от пользователя {}", bidderId);
            throw new IllegalArgumentException("Нельзя делать ставки на собственный аукцион");
        }

        // TODO: Проверяем наличие депозита у участника
        // if (!depositRepository.hasSufficientDeposit(bidderId, amount)) {
        //     throw new IllegalArgumentException("Недостаточно средств на депозите");
        // }

        boolean buyNowUsed = false;
        boolean auctionExtended = false;
        OffsetDateTime oldEndTime = auction.getEndTime();

        try {
            // Вызываем бизнес-логику добавления ставки с текущим временем
            auction.addBid(amount, bidderId, OffsetDateTime.now());

            // Проверяем, было ли продление
            if (auction.getEndTime().isAfter(oldEndTime)) {
                auctionExtended = true;
                log.info("Аукцион {} продлен! Новое время окончания: {}", auctionId, auction.getEndTime());
            }

            // Проверяем, была ли использована цена мгновенной покупки
            if (auction.getBuyNowPrice() != null && amount.compareTo(auction.getBuyNowPrice()) >= 0) {
                buyNowUsed = true;
                log.info("Аукцион завершен по цене мгновенной покупки! Победитель: {}", bidderId);
            } else {
                log.info("Ставка {} успешно принята, новая цена: {}, лидер: {}",
                        amount, auction.getCurrentPrice(), bidderId);
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Ошибка при создании ставки: {}", e.getMessage());
            throw e;
        }

        // Сохраняем изменения
        Auction saved = auctionRepository.save(auction);
        log.debug("Аукцион сохранен, версия: {}", saved.getVersion());

        // TODO: Если ставка выиграла (buyNowUsed), нужно списать средства с депозита
        // if (buyNowUsed) {
        //     depositRepository.withdraw(bidderId, amount);
        // }

        log.info("Ставка успешно создана! Итоговый статус аукциона: {}, продление: {}",
                saved.getStatus(), auctionExtended);

        return BidResponse.fromDomain(saved, amount, buyNowUsed, auctionExtended);
    }
}