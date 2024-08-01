package com.playtomic.tests.wallet.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.application.dto.TopUpRequestDto;
import com.playtomic.tests.wallet.application.port.PaymentService;
import com.playtomic.tests.wallet.infraestructure.database.JpaWalletRepository;
import com.playtomic.tests.wallet.infraestructure.database.entity.WalletDbEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
public class WalletIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaWalletRepository jpaWalletRepository;

    @MockBean
    private PaymentService paymentService;

    private UUID walletId;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        jpaWalletRepository.deleteAll();

        // Initialize test data
        walletId = UUID.randomUUID();
        var walletEntity = new WalletDbEntity(walletId, BigDecimal.valueOf(100.00));
        jpaWalletRepository.save(walletEntity);
    }

    @Test
    void testGetWallet() throws Exception {
        mockMvc.perform(get("/wallet/" + walletId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void testTopUp() throws Exception {
        var request = TopUpRequestDto.builder()
                .creditCardNumber("4111111111111111")
                .amount(BigDecimal.valueOf(50.00)).build();

        mockMvc.perform(post("/wallet/" + walletId + "/top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(150.00));
    }
}
