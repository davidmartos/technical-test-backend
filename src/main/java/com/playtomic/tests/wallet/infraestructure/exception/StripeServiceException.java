package com.playtomic.tests.wallet.infraestructure.exception;

public class StripeServiceException extends RuntimeException {
    public StripeServiceException(String message) {
        super(message);
    }
}
