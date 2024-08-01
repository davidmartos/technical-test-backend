package com.playtomic.tests.wallet.infraestructure.database.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "wallet")
public class WalletDbEntity {

    @Id
    private UUID id;

    private BigDecimal balance;

    @Version
    private Long version;
}
