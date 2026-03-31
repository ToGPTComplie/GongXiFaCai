package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.TradeTransactionRequestDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.service.TradeTransactionService;
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
class UserTradeTransactionControllerTest {

    @Mock
    private TradeTransactionService tradeTransactionService;

    @InjectMocks
    private UserTradeTransactionController userTradeTransactionController;

    @Test
    void processTrade_ReturnsResultWrappedData() {
        Long userId = 1L;
        TradeTransactionRequestDTO request = new TradeTransactionRequestDTO();
        request.setTicker("AAPL");
        request.setAssetType(Holding.AssetType.STOCK);
        request.setTransactionType(TradeTransaction.TransactionType.BUY);
        request.setQuantity(new BigDecimal("10.0"));
        request.setPrice(new BigDecimal("150.0"));

        User user = new User();
        user.setId(userId);

        TradeTransaction transaction = new TradeTransaction();
        transaction.setId(100L);
        transaction.setUser(user);
        transaction.setTicker("AAPL");
        transaction.setAssetType(Holding.AssetType.STOCK);
        transaction.setTransactionType(TradeTransaction.TransactionType.BUY);
        transaction.setQuantity(new BigDecimal("10.0"));
        transaction.setPrice(new BigDecimal("150.0"));
        transaction.setTotalAmount(new BigDecimal("1500.0"));
        transaction.setCreatedAt(LocalDateTime.now());

        when(tradeTransactionService.processTrade(
                userId,
                request.getTicker(),
                request.getAssetType(),
                request.getTransactionType(),
                request.getQuantity(),
                request.getPrice()
        )).thenReturn(transaction);

        Result<TradeTransactionResponseDTO> result = userTradeTransactionController.processTrade(userId, request);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Success", result.getMessage());

        TradeTransactionResponseDTO data = result.getData();
        assertNotNull(data);
        assertEquals(100L, data.getId());
        assertEquals(userId, data.getUserId());
        assertEquals("AAPL", data.getTicker());
        assertEquals(Holding.AssetType.STOCK, data.getAssetType());
        assertEquals(TradeTransaction.TransactionType.BUY, data.getTransactionType());
        assertEquals(new BigDecimal("10.0"), data.getQuantity());
        assertEquals(new BigDecimal("150.0"), data.getPrice());
        assertEquals(new BigDecimal("1500.0"), data.getTotalAmount());
    }
}