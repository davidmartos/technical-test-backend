package com.playtomic.tests.wallet.infraestructure.database;

import com.playtomic.tests.wallet.infraestructure.database.entity.WalletDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaWalletRepository extends JpaRepository<WalletDbEntity, UUID> {
    Optional<Object> findByIdAndTransactionIdempotencyKey(UUID id, String transactionIdempotencyKey);
}
