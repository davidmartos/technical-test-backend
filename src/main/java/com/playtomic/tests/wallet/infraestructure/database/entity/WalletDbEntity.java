package com.playtomic.tests.wallet.infraestructure.database.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "wallet")
public class WalletDbEntity {

    @Id
    private UUID id;

    private BigDecimal balance;
}
