package com.playtomic.tests.wallet.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class WalletDto {

    private UUID id;

    private BigDecimal balance;
}
