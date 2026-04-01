package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.HoldingRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import com.gongxifacai.gongxifacai.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingServiceImplTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private HoldingServiceImpl holdingService;

    private Long userId;
    private String ticker;
    private Holding existingHolding;

    @BeforeEach
    void setUp() {
        userId = 1L;
        ticker = "AAPL";

        User user = new User();
        user.setId(userId);

        existingHolding = new Holding();
        existingHolding.setId(100L);
        existingHolding.setUser(user);
        existingHolding.setTicker(ticker);
        existingHolding.setAssetType(Holding.AssetType.STOCK);
        existingHolding.setQuantity(new BigDecimal("10.0000"));
        existingHolding.setAverageCost(new BigDecimal("150.0000"));
    }

    @Test
    void getUserHoldings_ShouldReturnHoldingsList() {
        // Arrange
        when(holdingRepository.findByUser_Id(userId)).thenReturn(Arrays.asList(existingHolding));

        // Act
        List<Holding> result = holdingService.getUserHoldings(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ticker, result.get(0).getTicker());
        verify(holdingRepository, times(1)).findByUser_Id(userId);
    }

    @Test
    void getHolding_WhenExists_ShouldReturnHolding() {
        // Arrange
        when(holdingRepository.findByUser_Id(userId)).thenReturn(Arrays.asList(existingHolding));

        // Act
        Holding result = holdingService.getHolding(userId, ticker);

        // Assert
        assertNotNull(result);
        assertEquals(ticker, result.getTicker());
    }

    @Test
    void getHolding_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(holdingRepository.findByUser_Id(userId)).thenReturn(Collections.emptyList());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            holdingService.getHolding(userId, "TSLA");
        });
        assertEquals("持仓不存在", exception.getMessage());
    }

    @Test
    void getOrCreateHolding_WhenExists_ShouldReturnExistingWithoutSaving() {
        // Arrange
        when(holdingRepository.findByUser_Id(userId)).thenReturn(Arrays.asList(existingHolding));

        // Act
        Holding result = holdingService.getOrCreateHolding(userId, ticker, Holding.AssetType.STOCK);

        // Assert
        assertNotNull(result);
        assertEquals(existingHolding.getId(), result.getId());
        verify(userService, never()).getReferenceById(any());
        verify(holdingRepository, never()).save(any());
    }

    @Test
    void getOrCreateHolding_WhenNotExists_ShouldCreateAndSaveNewHolding() {
        // Arrange
        when(holdingRepository.findByUser_Id(userId)).thenReturn(Collections.emptyList());

        User proxyUser = new User();
        proxyUser.setId(userId);
        when(userService.getReferenceById(userId)).thenReturn(proxyUser);

        when(holdingRepository.save(any(Holding.class))).thenAnswer(invocation -> {
            Holding saved = invocation.getArgument(0);
            saved.setId(200L);
            return saved;
        });

        // Act
        Holding result = holdingService.getOrCreateHolding(userId, "TSLA", Holding.AssetType.STOCK);

        // Assert
        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals("TSLA", result.getTicker());
        assertEquals(Holding.AssetType.STOCK, result.getAssetType());
        assertEquals(BigDecimal.ZERO, result.getQuantity());
        assertEquals(BigDecimal.ZERO, result.getAverageCost());
        assertEquals(userId, result.getUser().getId());

        verify(userService, times(1)).getReferenceById(userId);
        verify(holdingRepository, times(1)).save(any(Holding.class));
    }
}
