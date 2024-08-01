package com.playtomic.tests.wallet.infraestructure.exception;

public class StripeAmountTooSmallException extends StripeServiceException {
    public StripeAmountTooSmallException(String message) {
        super(message);
    }
}
