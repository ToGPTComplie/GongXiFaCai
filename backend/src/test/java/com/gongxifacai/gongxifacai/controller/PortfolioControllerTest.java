package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.PortfolioPerformanceDTO;
import com.gongxifacai.gongxifacai.service.PortfolioSnapshotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

    @Mock
    private PortfolioSnapshotService portfolioSnapshotService;

    @InjectMocks
    private PortfolioController portfolioController;

    @Test
    void getPortfolioPerformance_HasData_ReturnsResultWrappedList() {
        // Arrange
        Long userId = 1L;
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 3, 31);

        PortfolioPerformanceDTO dto1 = new PortfolioPerformanceDTO();
        dto1.setDate(LocalDate.of(2026, 3, 30));
        dto1.setRealizedPnl(new BigDecimal("100.0000"));
        dto1.setUnrealizedPnl(new BigDecimal("50.0000"));
        dto1.setTotalPnl(new BigDecimal("150.0000"));

        PortfolioPerformanceDTO dto2 = new PortfolioPerformanceDTO();
        dto2.setDate(LocalDate.of(2026, 3, 31));
        dto2.setRealizedPnl(new BigDecimal("120.0000"));
        dto2.setUnrealizedPnl(new BigDecimal("30.0000"));
        dto2.setTotalPnl(new BigDecimal("150.0000"));

        when(portfolioSnapshotService.getPerformanceHistory(userId, start, end))
                .thenReturn(List.of(dto1, dto2));

        // Act
        Result<List<PortfolioPerformanceDTO>> result = portfolioController.getPortfolioPerformance(userId, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());

        assertEquals(LocalDate.of(2026, 3, 30), result.getData().get(0).getDate());
        assertEquals(0, new BigDecimal("150.0000").compareTo(result.getData().get(0).getTotalPnl()));

        assertEquals(LocalDate.of(2026, 3, 31), result.getData().get(1).getDate());
        assertEquals(0, new BigDecimal("150.0000").compareTo(result.getData().get(1).getTotalPnl()));
    }

    @Test
    void getPortfolioPerformance_NoData_ReturnsEmptyList() {
        // Arrange
        Long userId = 1L;
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        when(portfolioSnapshotService.getPerformanceHistory(userId, start, end))
                .thenReturn(List.of());

        // Act
        Result<List<PortfolioPerformanceDTO>> result = portfolioController.getPortfolioPerformance(userId, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }
}

