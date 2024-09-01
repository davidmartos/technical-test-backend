package com.playtomic.tests.wallet.infraestructure.controller;

import com.playtomic.tests.wallet.application.dto.TopUpRequestDto;
import com.playtomic.tests.wallet.application.dto.WalletDto;
import com.playtomic.tests.wallet.application.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallet")
@Tag(name = "Wallet", description = "Wallet management API")
public class WalletController {

    private final static Logger LOGGER = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a wallet by ID", description = "Retrieves wallet information based on the provided ID")
    public WalletDto getWallet(@PathVariable UUID id) {
        LOGGER.info("Received request to get wallet with ID: {}", id);
        var walletDto = walletService.getWallet(id);
        LOGGER.info("Returning wallet with ID: {} and balance: {}", walletDto.getId(), walletDto.getBalance());
        return walletDto;
    }

    @PostMapping("/{id}/top-up")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Top up a wallet", description = "Adds funds to the wallet using a credit card")
    public WalletDto topUp(@PathVariable UUID id, @RequestBody TopUpRequestDto request) {
        LOGGER.info("Received request to top up wallet with ID: {} and request: {}", id, request);
        var walletDto = walletService.topUp(id, request);
        LOGGER.info("Returning wallet with ID: {} and balance: {}", walletDto.getId(), walletDto.getBalance());
        return walletDto;
    }
}
