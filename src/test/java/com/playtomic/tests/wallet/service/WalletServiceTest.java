package com.playtomic.tests.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.application.dto.TopUpRequestDto;
import com.playtomic.tests.wallet.application.dto.WalletDto;
import com.playtomic.tests.wallet.application.port.PaymentService;
import com.playtomic.tests.wallet.application.service.WalletService;
import com.playtomic.tests.wallet.domain.exception.PaymentFailedException;
import com.playtomic.tests.wallet.domain.exception.WalletNotFoundException;
import com.playtomic.tests.wallet.domain.model.Wallet;
import com.playtomic.tests.wallet.domain.repository.WalletRepository;
import com.playtomic.tests.wallet.infraestructure.exception.StripeServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WalletServiceTest {

    @InjectMocks
    private WalletService walletService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ObjectMapper objectMapper;

    private UUID walletId;
    private BigDecimal originalBalance;
    private String idempotencyKey;
    private Wallet wallet;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        walletId = UUID.randomUUID();
        originalBalance = BigDecimal.valueOf(100.00);
        idempotencyKey = "test-key";
        wallet = Wallet.builder()
                .id(walletId)
                .balance(originalBalance)
                .transactionIdempotencyKey(idempotencyKey)
                .build();
    }

    @Test
    public void testGetWallet_Success() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        var walletDto = WalletDto.builder().id(walletId).balance(originalBalance).build();

        when(objectMapper.convertValue(wallet, WalletDto.class)).thenReturn(walletDto);

        var result = walletService.getWallet(walletId);

        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(originalBalance, result.getBalance());

        verify(walletRepository).findById(walletId);
    }

    @Test
    public void testGetWallet_NotFound() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getWallet(walletId));

        verify(walletRepository).findById(walletId);
    }

    @Test
    public void testTopUp_Success() throws StripeServiceException {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        doNothing().when(paymentService).charge(anyString(), any(BigDecimal.class));

        var amountToTopUp = BigDecimal.valueOf(50.00);
        var newIdempotencyKey = UUID.randomUUID().toString();
        var request = createTopUpRequest(amountToTopUp, newIdempotencyKey);

        var expectedBalance = originalBalance.add(amountToTopUp);
        var expectedWalletDto = WalletDto.builder().id(walletId).balance(expectedBalance).build();

        when(walletRepository.save(wallet)).thenReturn(wallet);
        when(objectMapper.convertValue(wallet, WalletDto.class)).thenReturn(expectedWalletDto);

        var result = walletService.topUp(walletId, request);

        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(expectedBalance, result.getBalance());

        verify(paymentService).charge(request.getCreditCardNumber(), request.getAmount());
    }

    @Test
    public void testTopUp_PaymentFailed() throws StripeServiceException {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        doThrow(new StripeServiceException("Payment processing failed")).when(paymentService).charge(anyString(), any(BigDecimal.class));

        var request = createTopUpRequest(BigDecimal.valueOf(50.00), idempotencyKey);

        assertThrows(PaymentFailedException.class, () -> walletService.topUp(walletId, request));

        verify(paymentService).charge(request.getCreditCardNumber(), request.getAmount());
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    public void testTopUp_WalletNotFound() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        var request = createTopUpRequest(BigDecimal.valueOf(50.00), idempotencyKey);

        assertThrows(WalletNotFoundException.class, () -> walletService.topUp(walletId, request));

        verify(walletRepository).findById(walletId);
        verify(paymentService, never()).charge(anyString(), any(BigDecimal.class));
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    public void testTopUp_IdempotencyCheck() {
        when(walletRepository.findByIdAndTransactionIdempotencyKey(any(UUID.class), anyString())).thenReturn(Optional.of(wallet));

        var request = TopUpRequestDto.builder()
                .creditCardNumber("4111111111111111")
                .amount(BigDecimal.valueOf(50.00))
                .idempotencyKey(idempotencyKey)
                .build();

        var expectedWalletDto = WalletDto.builder()
                .id(walletId)
                .balance(BigDecimal.valueOf(100.00))
                .build();

        when(objectMapper.convertValue(any(Wallet.class), eq(WalletDto.class)))
                .thenReturn(expectedWalletDto);

        var result = walletService.topUp(walletId, request);

        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(BigDecimal.valueOf(100.00), result.getBalance());

        verify(paymentService, never()).charge(anyString(), any(BigDecimal.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    private TopUpRequestDto createTopUpRequest(BigDecimal amount, String idempotencyKey) {
        return TopUpRequestDto.builder().creditCardNumber("4111111111111111").amount(amount).idempotencyKey(idempotencyKey).build();
    }
}
