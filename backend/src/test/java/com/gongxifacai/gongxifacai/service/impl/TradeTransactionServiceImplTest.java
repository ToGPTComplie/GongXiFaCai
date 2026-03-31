package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.PageResponseDTO;
import com.gongxifacai.gongxifacai.dto.TradeTransactionResponseDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.TradeTransactionRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeTransactionServiceImplTest {

    @Mock
    private TradeTransactionRepository tradeTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TradeTransactionServiceImpl tradeTransactionService;

    private User testUser;
    private TradeTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setAvailableCash(new BigDecimal("1000.00"));

        testTransaction = new TradeTransaction();
        testTransaction.setId(200L);
        testTransaction.setUser(testUser);
        testTransaction.setTicker("AAPL");
        testTransaction.setAssetType(Holding.AssetType.STOCK);
        testTransaction.setTransactionType(TradeTransaction.TransactionType.BUY);
        testTransaction.setQuantity(new BigDecimal("10.00"));
        testTransaction.setPrice(new BigDecimal("150.00"));
        testTransaction.setTotalAmount(new BigDecimal("1500.00"));
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getUserTradeTransactions_UserExists_ReturnsPage() {
        // Arrange
        Long userId = 1L;
        int page = 0;
        int size = 10;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TradeTransaction> transactionPage = new PageImpl<>(List.of(testTransaction), pageable, 1);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(tradeTransactionRepository.findByUser_Id(eq(userId), any(PageRequest.class))).thenReturn(transactionPage);

        // Act
        PageResponseDTO<TradeTransactionResponseDTO> response = tradeTransactionService.getUserTradeTransactions(userId, page, size);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        TradeTransactionResponseDTO dto = response.getContent().get(0);
        assertEquals(testTransaction.getId(), dto.getId());
        assertEquals(testTransaction.getTicker(), dto.getTicker());
        assertEquals(testTransaction.getTotalAmount(), dto.getTotalAmount());
        assertEquals(testTransaction.getTransactionType(), dto.getTransactionType());
        assertEquals(testTransaction.getAssetType(), dto.getAssetType());
    }

    @Test
    void getUserTradeTransactions_UserDoesNotExist_ThrowsBusinessException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeTransactionService.getUserTradeTransactions(userId, 0, 10);
        });

        assertEquals(CommonErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }
}
