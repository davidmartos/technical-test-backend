package com.playtomic.tests.wallet.infraestructure.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.domain.model.Wallet;
import com.playtomic.tests.wallet.domain.repository.WalletRepository;
import com.playtomic.tests.wallet.infraestructure.database.entity.WalletDbEntity;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    private final ObjectMapper objectMapper;
    private final JpaWalletRepository jpaWalletRepository;

    @Override
    @Cacheable(value = "wallet", key = "#id")
    public Optional<Wallet> findById(UUID id) {
        return jpaWalletRepository.findById(id).map(walletDbEntity -> objectMapper.convertValue(walletDbEntity, Wallet.class));
    }

    @Override
    @CachePut(value = "wallet", key = "#wallet.id")
    @Retryable(retryFor = {OptimisticLockingFailureException.class, DataAccessException.class},
            backoff = @Backoff(delay = 100))
    public Wallet save(Wallet wallet) {
        var dbEntity = objectMapper.convertValue(wallet, WalletDbEntity.class);
        var savedEntity = jpaWalletRepository.save(dbEntity);
        return objectMapper.convertValue(savedEntity, Wallet.class);
    }

    @Override
    public Optional<Wallet> findByIdAndTransactionIdempotencyKey(UUID id, String idempotencyKey) {
        return jpaWalletRepository.findByIdAndTransactionIdempotencyKey(id, idempotencyKey)
                .map(walletDbEntity -> objectMapper.convertValue(walletDbEntity, Wallet.class));
    }
}
