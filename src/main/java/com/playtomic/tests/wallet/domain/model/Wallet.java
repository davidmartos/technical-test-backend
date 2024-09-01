package com.playtomic.tests.wallet.domain.model;

import com.playtomic.tests.wallet.domain.exception.InsufficientBalanceException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    private UUID id;
    private BigDecimal balance;
    private String transactionIdempotencyKey;
    private Long version;

    public void topUp(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void spend(BigDecimal amount) {
        if (this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
        } else {
            throw new InsufficientBalanceException("Not enough balance");
        }
    }

    public void refund(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
