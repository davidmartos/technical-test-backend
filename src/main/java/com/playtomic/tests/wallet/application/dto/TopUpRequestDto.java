package com.playtomic.tests.wallet.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.math.BigDecimal;

@Data
@Builder
public class TopUpRequestDto {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Credit card number is required")
    @CreditCardNumber(message = "Invalid credit card number")
    private String creditCardNumber;

    @NotNull(message = "Idempotency Key is required")
    private String idempotencyKey;
}
