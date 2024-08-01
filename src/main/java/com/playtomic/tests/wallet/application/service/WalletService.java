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
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class WalletService {

    private final static Logger LOGGER = LoggerFactory.getLogger(WalletController.class);

    private final ObjectMapper objectMapper;
    private final WalletRepository walletRepository;
    private final PaymentService paymentService;

    public WalletDto getWallet(UUID walletId) {
        var wallet = findWalletById(walletId);
        return objectMapper.convertValue(wallet, WalletDto.class);
    }

    public WalletDto topUp(UUID walletId, TopUpRequestDto request) {
        var wallet = findWalletById(walletId);
        try {
            paymentService.charge(request.getCreditCardNumber(), request.getAmount());

            wallet.topUp(request.getAmount());
            walletRepository.save(wallet);

            return objectMapper.convertValue(wallet, WalletDto.class);

        } catch (StripeServiceException e) {
            LOGGER.error("Payment failed for walletId: {} with error: {}", walletId, e.getMessage());
            throw new PaymentFailedException("Payment failed: " + e.getMessage());
        }

    }

    private Wallet findWalletById(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }
}
