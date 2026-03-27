package com.example.autoauction.deposit.web;

import com.example.autoauction.auth.domain.UserPrincipal;
import com.example.autoauction.auth.infrastructure.security.CurrentUser;
import com.example.autoauction.deposit.application.DepositService;
import com.example.autoauction.deposit.web.dto.DepositRequest;
import com.example.autoauction.deposit.web.dto.DepositResponse;
import com.example.autoauction.deposit.web.dto.DepositTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/deposits")
@Tag(name = "Депозиты", description = "Управление депозитами пользователей")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
        log.info("DepositController initialized");
    }
    @PostMapping("/deposit")
    @Operation(summary = "Пополнить депозит")
    public ResponseEntity<DepositResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal currentUser
    ) {
        Long userId = currentUser.getUserId();
        log.info("User {} depositing: {}", userId, request.amount());

        DepositResponse response = depositService.deposit(userId, request.amount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Вывести средства с депозита")
    public ResponseEntity<DepositResponse> withdraw(
            @Valid @RequestBody DepositRequest request,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal currentUser
    ) {
        Long userId = currentUser.getUserId();
        log.info("User {} withdrawing: {}", userId, request.amount());

        DepositResponse response = depositService.withdraw(userId, request.amount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мой депозит")
    public ResponseEntity<DepositResponse> getMyDeposit(
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal currentUser
    ) {
        Long userId = currentUser.getUserId();
        log.debug("User {} getting deposit info", userId);

        DepositResponse response = depositService.getDepositByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/transactions")
    @Operation(summary = "Получить мои транзакции")
    public List<DepositTransactionResponse> getMyTransactions(
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal currentUser
    ) {
        Long userId = currentUser.getUserId();
        log.debug("User {} getting transactions", userId);

        return depositService.getTransactionsByUserId(userId);
    }

    @GetMapping("/can-participate")
    @Operation(summary = "Проверить возможность участия в аукционе")
    public ResponseEntity<ParticipationResponse> canParticipate(
            @RequestParam(required = false) BigDecimal startingPrice,
            @Parameter(hidden = true)
            @CurrentUser UserPrincipal currentUser
    ) {
        Long userId = currentUser.getUserId();
        boolean canParticipate;

        if (startingPrice != null) {
            canParticipate = depositService.canParticipateInAuction(userId, startingPrice);
        } else {
            canParticipate = depositService.canParticipateInAuction(userId);
        }

        return ResponseEntity.ok(new ParticipationResponse(canParticipate));
    }

    public record ParticipationResponse(boolean canParticipate) {}
}