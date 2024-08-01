package com.playtomic.tests.wallet.service;


import com.playtomic.tests.wallet.domain.model.Payment;
import com.playtomic.tests.wallet.infraestructure.client.StripeService;
import com.playtomic.tests.wallet.infraestructure.exception.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.infraestructure.exception.StripeRestTemplateResponseErrorHandler;
import com.playtomic.tests.wallet.infraestructure.exception.StripeServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test is failing with the current implementation.
 * <p>
 * How would you test this?
 */
public class StripeServiceTest {

    private URI testUri;

    private RestTemplate restTemplate;

    private StripeService stripeService;

    @BeforeEach
    public void setUp() {
        testUri = URI.create("http://how-would-you-test-me.localhost");
        restTemplate = mock(RestTemplate.class);
        var restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.errorHandler(any(StripeRestTemplateResponseErrorHandler.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        stripeService = new StripeService(testUri, testUri, restTemplateBuilder);
    }

    @Test
    public void test_charge_throwsStripeAmountTooSmallException() {
        Assertions.assertThrows(StripeAmountTooSmallException.class, () -> {
            when(restTemplate.postForObject(eq(testUri), any(), eq(Payment.class)))
                    .thenThrow(new StripeAmountTooSmallException("Stripe Amount is Too Small"));

            stripeService.charge("4242 4242 4242 4242", BigDecimal.valueOf(5));
        });
    }

    @Test
    public void test_charge_success() throws StripeServiceException {
        when(restTemplate.postForObject(eq(testUri), any(), eq(Payment.class)))
                .thenReturn(Payment.builder().build());

        stripeService.charge("4242 4242 4242 4242", BigDecimal.valueOf(15));
    }
}
