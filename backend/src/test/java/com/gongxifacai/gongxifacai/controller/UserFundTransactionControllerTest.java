package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.FundTransactionRequestDTO;
import com.gongxifacai.gongxifacai.dto.FundTransactionResponseDTO;
import com.gongxifacai.gongxifacai.entity.FundTransaction;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.service.FundTransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFundTransactionControllerTest {

    @Mock
    private FundTransactionService fundTransactionService;

    @InjectMocks
    private UserFundTransactionController userFundTransactionController;

    @Test
    void processFundTransaction_ReturnsResultWrappedData() {
        Long userId = 1L;
        FundTransactionRequestDTO request = new FundTransactionRequestDTO();
        request.setTransactionType(FundTransaction.FundTransactionType.DEPOSIT);
        request.setAmount(new BigDecimal("1000.0"));
        request.setDescription("Bank Transfer");

        User user = new User();
        user.setId(userId);

        FundTransaction transaction = new FundTransaction();
        transaction.setId(200L);
        transaction.setUser(user);
        transaction.setTransactionType(FundTransaction.FundTransactionType.DEPOSIT);
        transaction.setTotalAmount(new BigDecimal("1000.0"));
        transaction.setDescription("Bank Transfer");
        transaction.setCreatedAt(LocalDateTime.now());

        when(fundTransactionService.processFundTransaction(
                userId,
                request.getTransactionType(),
                request.getAmount(),
                request.getDescription()
        )).thenReturn(transaction);

        Result<FundTransactionResponseDTO> result = userFundTransactionController.processFundTransaction(userId, request);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Success", result.getMessage());

        FundTransactionResponseDTO data = result.getData();
        assertNotNull(data);
        assertEquals(200L, data.getId());
        assertEquals(userId, data.getUserId());
        assertEquals(FundTransaction.FundTransactionType.DEPOSIT, data.getTransactionType());
        assertEquals(new BigDecimal("1000.0"), data.getTotalAmount());
        assertEquals("Bank Transfer", data.getDescription());
    }
}