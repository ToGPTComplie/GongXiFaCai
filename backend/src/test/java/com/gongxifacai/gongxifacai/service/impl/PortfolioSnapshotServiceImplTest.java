package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.PortfolioPerformanceDTO;
import com.gongxifacai.gongxifacai.entity.PortfolioSnapshot;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.PortfolioSnapshotRepository;
import com.gongxifacai.gongxifacai.repository.TradeTransactionRepository;
import com.gongxifacai.gongxifacai.service.UnrealizedPnlService;
import com.gongxifacai.gongxifacai.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioSnapshotServiceImplTest {

    @Mock
    private PortfolioSnapshotRepository portfolioSnapshotRepository;

    @Mock
    private TradeTransactionRepository tradeTransactionRepository;

    @Mock
    private UnrealizedPnlService unrealizedPnlService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PortfolioSnapshotServiceImpl portfolioSnapshotService;

    private User testUser;
    private final LocalDate testDate = LocalDate.of(2026, 3, 31);

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setAvailableCash(new BigDecimal("5000.0000"));
    }

    // -------------------------------------------------------
    // createOrUpdateSnapshot
    // -------------------------------------------------------

    @Test
    void createOrUpdateSnapshot_NoExistingSnapshot_CreatesNewAndSaves() {
        // Arrange
        Long userId = 1L;
        BigDecimal realizedPnl = new BigDecimal("100.0000");
        BigDecimal unrealizedPnl = new BigDecimal("50.0000");

        when(tradeTransactionRepository.sumRealizedPnlByUserId(userId, TradeTransaction.TransactionType.SELL))
                .thenReturn(realizedPnl);
        when(unrealizedPnlService.getTotalUnrealizedPnl(userId))
                .thenReturn(unrealizedPnl);
        when(userService.getReferenceById(userId))
                .thenReturn(testUser);
        when(portfolioSnapshotRepository.findByUser_IdAndSnapshotDate(userId, testDate))
                .thenReturn(Optional.empty());
        when(portfolioSnapshotRepository.save(any(PortfolioSnapshot.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        // Act
        portfolioSnapshotService.createOrUpdateSnapshot(userId, testDate);

        // Assert：捕获 save 入参，验证字段值
        ArgumentCaptor<PortfolioSnapshot> captor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(portfolioSnapshotRepository).save(captor.capture());

        PortfolioSnapshot saved = captor.getValue();
        assertEquals(testDate, saved.getSnapshotDate());
        assertEquals(testUser, saved.getUser());
        assertEquals(0, realizedPnl.compareTo(saved.getRealizedPnl()));
        assertEquals(0, unrealizedPnl.compareTo(saved.getUnrealizedPnl()));
        assertEquals(0, new BigDecimal("150.0000").compareTo(saved.getTotalPnl())); // 100 + 50
    }

    @Test
    void createOrUpdateSnapshot_ExistingSnapshot_UpdatesValues() {
        // Arrange：当天已有快照（旧数据）
        Long userId = 1L;
        PortfolioSnapshot existing = new PortfolioSnapshot();
        existing.setId(1L); // 模拟从数据库查回来的对象，id 不为 null
        existing.setUser(testUser);
        existing.setSnapshotDate(testDate);
        existing.setRealizedPnl(new BigDecimal("80.0000"));
        existing.setUnrealizedPnl(new BigDecimal("20.0000"));
        existing.setTotalPnl(new BigDecimal("100.0000"));

        BigDecimal newRealized = new BigDecimal("120.0000");
        BigDecimal newUnrealized = new BigDecimal("30.0000");

        when(tradeTransactionRepository.sumRealizedPnlByUserId(userId, TradeTransaction.TransactionType.SELL))
                .thenReturn(newRealized);
        when(unrealizedPnlService.getTotalUnrealizedPnl(userId))
                .thenReturn(newUnrealized);
        when(portfolioSnapshotRepository.findByUser_IdAndSnapshotDate(userId, testDate))
                .thenReturn(Optional.of(existing));
        when(portfolioSnapshotRepository.save(any(PortfolioSnapshot.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        // Act
        portfolioSnapshotService.createOrUpdateSnapshot(userId, testDate);

        // Assert：更新旧快照，不重新赋 user 和 snapshotDate
        ArgumentCaptor<PortfolioSnapshot> captor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(portfolioSnapshotRepository).save(captor.capture());

        PortfolioSnapshot saved = captor.getValue();
        assertEquals(0, newRealized.compareTo(saved.getRealizedPnl()));
        assertEquals(0, newUnrealized.compareTo(saved.getUnrealizedPnl()));
        assertEquals(0, new BigDecimal("150.0000").compareTo(saved.getTotalPnl())); // 120 + 30
        // 更新场景不应再调 getReferenceById（因为 user 已存在）
        verify(userService, never()).getReferenceById(any());
    }

    // -------------------------------------------------------
    // getPerformanceHistory
    // -------------------------------------------------------

    @Test
    void getPerformanceHistory_UserExists_ReturnsMappedDTOs() {
        // Arrange
        Long userId = 1L;
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 3, 31);

        PortfolioSnapshot snapshot1 = new PortfolioSnapshot();
        snapshot1.setSnapshotDate(LocalDate.of(2026, 3, 30));
        snapshot1.setRealizedPnl(new BigDecimal("80.0000"));
        snapshot1.setUnrealizedPnl(new BigDecimal("20.0000"));
        snapshot1.setTotalPnl(new BigDecimal("100.0000"));

        PortfolioSnapshot snapshot2 = new PortfolioSnapshot();
        snapshot2.setSnapshotDate(LocalDate.of(2026, 3, 31));
        snapshot2.setRealizedPnl(new BigDecimal("100.0000"));
        snapshot2.setUnrealizedPnl(new BigDecimal("50.0000"));
        snapshot2.setTotalPnl(new BigDecimal("150.0000"));

        when(userService.existsById(userId)).thenReturn(true);
        when(portfolioSnapshotRepository
                .findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, start, end))
                .thenReturn(List.of(snapshot1, snapshot2));

        // Act
        List<PortfolioPerformanceDTO> result = portfolioSnapshotService.getPerformanceHistory(userId, start, end);

        // Assert
        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2026, 3, 30), result.get(0).getDate());
        assertEquals(0, new BigDecimal("100.0000").compareTo(result.get(0).getTotalPnl()));
        assertEquals(LocalDate.of(2026, 3, 31), result.get(1).getDate());
        assertEquals(0, new BigDecimal("150.0000").compareTo(result.get(1).getTotalPnl()));
    }

    @Test
    void getPerformanceHistory_UserNotFound_ThrowsBusinessException() {
        // Arrange
        Long userId = 999L;
        when(userService.existsById(userId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () ->
                portfolioSnapshotService.getPerformanceHistory(userId, testDate, testDate));

        assertEquals(CommonErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void getPerformanceHistory_NoSnapshotsInRange_ReturnsEmptyList() {
        // Arrange
        Long userId = 1L;
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        when(userService.existsById(userId)).thenReturn(true);
        when(portfolioSnapshotRepository
                .findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, start, end))
                .thenReturn(List.of());

        // Act
        List<PortfolioPerformanceDTO> result = portfolioSnapshotService.getPerformanceHistory(userId, start, end);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}


