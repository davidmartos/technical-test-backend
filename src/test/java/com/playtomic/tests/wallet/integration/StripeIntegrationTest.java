package com.playtomic.tests.wallet.integration;

import com.playtomic.tests.wallet.domain.model.Payment;
import com.playtomic.tests.wallet.infraestructure.client.StripeService;
import com.playtomic.tests.wallet.infraestructure.exception.StripeServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class StripeIntegrationTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${stripe.simulator.charges-uri}")
    private URI chargesUri;

    @Value("${stripe.simulator.refunds-uri}")
    private URI refundsUri;

    private StripeService stripeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(restTemplateBuilder.errorHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        stripeService = new StripeService(
                chargesUri,
                refundsUri,
                restTemplateBuilder
        );
    }

    @Test
    public void testChargeSuccess() {
        Payment payment = Payment.builder().id("ch_123456").build();

        when(restTemplate.postForObject(any(URI.class), any(), eq(Payment.class)))
                .thenReturn(payment);

        assertDoesNotThrow(() -> stripeService.charge("4242424242424242", BigDecimal.valueOf(15.00)));

        verify(restTemplate).postForObject(any(URI.class), any(), eq(Payment.class));
    }

    @Test
    public void testChargeFailure_ServerError() {
        when(restTemplate.postForObject(any(URI.class), any(), eq(Payment.class)))
                .thenReturn(null);

        assertThrows(StripeServiceException.class,
                () -> stripeService.charge("4242424242424242", BigDecimal.valueOf(20.00)));

        verify(restTemplate).postForObject(any(URI.class), any(), eq(Payment.class));
    }

}