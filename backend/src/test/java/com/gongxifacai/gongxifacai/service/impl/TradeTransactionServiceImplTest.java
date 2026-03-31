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
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.service.HoldingService;
import com.gongxifacai.gongxifacai.repository.HoldingRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeTransactionServiceImplTest {

    @Mock
    private TradeTransactionRepository tradeTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private UserService userService;

    @Mock
    private HoldingService holdingService;

    @InjectMocks
    private TradeTransactionServiceImpl tradeTransactionService;

    private User testUser;
    private TradeTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setAvailableCash(new BigDecimal("1000.0000"));

        testTransaction = new TradeTransaction();
        testTransaction.setId(200L);
        testTransaction.setUser(testUser);
        testTransaction.setTicker("AAPL");
        testTransaction.setAssetType(Holding.AssetType.STOCK);
        testTransaction.setTransactionType(TradeTransaction.TransactionType.BUY);
        testTransaction.setQuantity(new BigDecimal("10.0000"));
        testTransaction.setPrice(new BigDecimal("150.0000"));
        testTransaction.setTotalAmount(new BigDecimal("1500.0000"));
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

        when(userService.existsById(userId)).thenReturn(true);
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
        assertEquals(0, testTransaction.getTotalAmount().compareTo(dto.getTotalAmount()));
        assertEquals(testTransaction.getTransactionType(), dto.getTransactionType());
        assertEquals(testTransaction.getAssetType(), dto.getAssetType());
    }

    @Test
    void getUserTradeTransactions_UserDoesNotExist_ThrowsBusinessException() {
        // Arrange
        Long userId = 999L;
        when(userService.existsById(userId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeTransactionService.getUserTradeTransactions(userId, 0, 10);
        });

        assertEquals(CommonErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void processTrade_Buy_Success() {
        Long userId = 1L;
        String ticker = "AAPL";
        BigDecimal quantity = new BigDecimal("10.0000");
        BigDecimal price = new BigDecimal("50.0000");
        BigDecimal totalAmount = new BigDecimal("500.0000");

        when(userService.getUser(userId)).thenReturn(testUser);
        when(tradeTransactionRepository.save(any(TradeTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TradeTransaction result = tradeTransactionService.processTrade(userId, ticker, Holding.AssetType.STOCK, TradeTransaction.TransactionType.BUY, quantity, price);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("500.0000").compareTo(testUser.getAvailableCash())); // 1000 - 500

        assertEquals(TradeTransaction.TransactionType.BUY, result.getTransactionType());
        assertEquals(0, totalAmount.compareTo(result.getTotalAmount()));
        assertEquals(0, quantity.compareTo(result.getQuantity()));
        assertEquals(0, price.compareTo(result.getPrice()));

        verify(userService).save(testUser);
        verify(holdingService).applyBuy(eq(userId), eq(ticker), eq(Holding.AssetType.STOCK), any(BigDecimal.class), any(BigDecimal.class));
        verify(tradeTransactionRepository).save(any(TradeTransaction.class));
    }

    @Test
    void processTrade_Sell_Success() {
        Long userId = 1L;
        String ticker = "AAPL";
        BigDecimal quantity = new BigDecimal("10.0000");
        BigDecimal price = new BigDecimal("150.0000");
        BigDecimal totalAmount = new BigDecimal("1500.0000");

        when(userService.getUser(userId)).thenReturn(testUser);
        when(tradeTransactionRepository.save(any(TradeTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TradeTransaction result = tradeTransactionService.processTrade(userId, ticker, Holding.AssetType.STOCK, TradeTransaction.TransactionType.SELL, quantity, price);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("2500.0000").compareTo(testUser.getAvailableCash())); // 1000 + 1500

        assertEquals(TradeTransaction.TransactionType.SELL, result.getTransactionType());
        assertEquals(0, totalAmount.compareTo(result.getTotalAmount()));
        assertEquals(0, quantity.compareTo(result.getQuantity()));
        assertEquals(0, price.compareTo(result.getPrice()));

        verify(userService).save(testUser);
        verify(holdingService).applySell(eq(userId), eq(ticker), any(BigDecimal.class));
        verify(tradeTransactionRepository).save(any(TradeTransaction.class));
    }

    @Test
    void processTrade_InvalidQuantityOrPrice() {
        Long userId = 1L;

        BusinessException exception1 = assertThrows(BusinessException.class, () -> {
            tradeTransactionService.processTrade(userId, "AAPL", Holding.AssetType.STOCK, TradeTransaction.TransactionType.BUY, BigDecimal.ZERO, new BigDecimal("100.0000"));
        });
        assertEquals(CommonErrorCode.BAD_REQUEST.getCode(), exception1.getCode());

        BusinessException exception2 = assertThrows(BusinessException.class, () -> {
            tradeTransactionService.processTrade(userId, "AAPL", Holding.AssetType.STOCK, TradeTransaction.TransactionType.BUY, new BigDecimal("10.0000"), BigDecimal.ZERO);
        });
        assertEquals(CommonErrorCode.BAD_REQUEST.getCode(), exception2.getCode());
    }

    @Test
    void processTrade_Buy_InsufficientBalance() {
        Long userId = 1L;
        String ticker = "AAPL";
        BigDecimal quantity = new BigDecimal("10.0000");
        BigDecimal price = new BigDecimal("150.0000"); // 1500 total, but user only has 1000

        when(userService.getUser(userId)).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeTransactionService.processTrade(userId, ticker, Holding.AssetType.STOCK, TradeTransaction.TransactionType.BUY, quantity, price);
        });

        assertEquals("余额不足", exception.getMessage());
    }

    @Test
    void processTrade_Sell_InsufficientHolding() {
        Long userId = 1L;
        String ticker = "AAPL";
        BigDecimal quantity = new BigDecimal("20.0000");
        BigDecimal price = new BigDecimal("150.0000");

        when(userService.getUser(userId)).thenReturn(testUser);
        when(holdingService.applySell(eq(userId), eq(ticker), any(BigDecimal.class))).thenThrow(new BusinessException("持仓不足"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeTransactionService.processTrade(userId, ticker, Holding.AssetType.STOCK, TradeTransaction.TransactionType.SELL, quantity, price);
        });

        assertEquals("持仓不足", exception.getMessage());
    }
}
