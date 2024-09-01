package com.playtomic.tests.wallet.domain.repository;

import com.playtomic.tests.wallet.domain.model.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Optional<Wallet> findById(UUID id);

    Wallet save(Wallet wallet);

    Optional<Wallet> findByIdAndTransactionIdempotencyKey(UUID id, String idempotencyKey);
}
