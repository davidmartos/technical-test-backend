package com.playtomic.tests.wallet.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TopUpRequestDto {

    private BigDecimal amount;

    private String creditCardNumber;
}
