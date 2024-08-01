package com.playtomic.tests.wallet.application.port;

import lombok.NonNull;

import java.math.BigDecimal;

public interface PaymentService {

    void charge(String creditCardNumber, BigDecimal amount);

    void refund(@NonNull String paymentId);
}
