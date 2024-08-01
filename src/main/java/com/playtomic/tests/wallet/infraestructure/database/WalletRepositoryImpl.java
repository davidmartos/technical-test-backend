package com.playtomic.tests.wallet.infraestructure.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.domain.model.Wallet;
import com.playtomic.tests.wallet.domain.repository.WalletRepository;
import com.playtomic.tests.wallet.infraestructure.database.entity.WalletDbEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    private final ObjectMapper objectMapper;
    private final JpaWalletRepository jpaWalletRepository;

    @Override
    public Optional<Wallet> findById(UUID id) {
        return jpaWalletRepository.findById(id).map(walletDbEntity -> objectMapper.convertValue(walletDbEntity, Wallet.class));
    }

    @Override
    public Wallet save(Wallet wallet) {
        var dbEntity = objectMapper.convertValue(wallet, WalletDbEntity.class);
        var savedEntity = jpaWalletRepository.save(dbEntity);
        return objectMapper.convertValue(savedEntity, Wallet.class);
    }
}
