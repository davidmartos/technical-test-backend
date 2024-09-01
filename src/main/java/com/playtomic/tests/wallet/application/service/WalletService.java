package com.playtomic.tests.wallet.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.application.dto.TopUpRequestDto;
import com.playtomic.tests.wallet.application.dto.WalletDto;
import com.playtomic.tests.wallet.application.port.PaymentService;
import com.playtomic.tests.wallet.domain.exception.PaymentFailedException;
import com.playtomic.tests.wallet.domain.exception.WalletNotFoundException;
import com.playtomic.tests.wallet.domain.model.Wallet;
import com.playtomic.tests.wallet.domain.repository.WalletRepository;
import com.playtomic.tests.wallet.infraestructure.controller.WalletController;
import com.playtomic.tests.wallet.infraestructure.exception.StripeServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class WalletService {

    private final static Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    private final static String PAYMENT_SERVICE = "paymentService";

    private final ObjectMapper objectMapper;
    private final WalletRepository walletRepository;
    private final PaymentService paymentService;

    public WalletDto getWallet(UUID walletId) {
        var wallet = findWalletById(walletId);
        return objectMapper.convertValue(wallet, WalletDto.class);
    }


    @CircuitBreaker(name = PAYMENT_SERVICE, fallbackMethod = "handlePaymentServiceFailure")
    @Retry(name = PAYMENT_SERVICE, fallbackMethod = "handlePaymentServiceFailure")
    @Transactional
    public WalletDto topUp(UUID walletId, TopUpRequestDto request) {

        // Verificar la existencia de una transacción idempotente
        var existingWallet = walletRepository.findByIdAndTransactionIdempotencyKey(walletId, request.getIdempotencyKey());

        if (existingWallet.isPresent()) {
            LOGGER.info("Idempotent request detected, returning existing result for walletId: {} with idempotencyKey: {}", walletId, request.getIdempotencyKey());
            return objectMapper.convertValue(existingWallet.get(), WalletDto.class);
        }

        var wallet = existingWallet.orElseGet(() -> findWalletById(walletId));

        try {
            paymentService.charge(request.getCreditCardNumber(), request.getAmount());

            wallet.topUp(request.getAmount());
            wallet.setTransactionIdempotencyKey(request.getIdempotencyKey());

            var savedWallet = walletRepository.save(wallet);

            return objectMapper.convertValue(savedWallet, WalletDto.class);

        } catch (StripeServiceException e) {
            LOGGER.error("Payment failed for walletId: {} with error: {}", walletId, e.getMessage());
            throw new PaymentFailedException("Payment failed: " + e.getMessage());
        }

    }

    private WalletDto handlePaymentServiceFailure(UUID walletId, Exception ex) {
        LOGGER.error("Payment service failed after retries for walletId: {}. Reason: {}", walletId, ex.getMessage());
        throw new PaymentFailedException("Payment service is currently unavailable, please try again later.");
    }

    private Wallet findWalletById(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }
}
