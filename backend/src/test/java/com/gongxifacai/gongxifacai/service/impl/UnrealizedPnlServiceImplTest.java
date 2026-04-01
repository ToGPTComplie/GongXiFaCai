package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.service.HoldingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnrealizedPnlServiceImplTest {

    @Mock
    private HoldingService holdingService;

    @InjectMocks
    private UnrealizedPnlServiceImpl unrealizedPnlService;

    /** 构造带 pl 的 HoldingDTO */
    private HoldingDTO makeDto(String ticker, BigDecimal pl) {
        HoldingDTO dto = new HoldingDTO();
        dto.setTicker(ticker);
        dto.setAssetType(Holding.AssetType.STOCK);
        dto.setQuantity(new BigDecimal("10"));
        dto.setAverageCost(new BigDecimal("100.00"));
        dto.setPl(pl);
        return dto;
    }

    // -------------------------------------------------------
    // 正常场景：多个持仓，返回 pl 之和
    // -------------------------------------------------------
    @Test
    void getTotalUnrealizedPnl_MultipleHoldings_ReturnsSumOfPl() {
        // AAPL 浮盈 +200，TSLA 浮亏 -50 → 合计 +150
        when(holdingService.getUserHoldingsWithprice(1L))
                .thenReturn(List.of(
                        makeDto("AAPL", new BigDecimal("200.00")),
                        makeDto("TSLA", new BigDecimal("-50.00"))
                ));

        BigDecimal result = unrealizedPnlService.getTotalUnrealizedPnl(1L);

        assertEquals(0, new BigDecimal("150.00").compareTo(result));
    }

    // -------------------------------------------------------
    // 正常场景：只有一个持仓
    // -------------------------------------------------------
    @Test
    void getTotalUnrealizedPnl_SingleHolding_ReturnsThatPl() {
        when(holdingService.getUserHoldingsWithprice(1L))
                .thenReturn(List.of(makeDto("AAPL", new BigDecimal("300.00"))));

        BigDecimal result = unrealizedPnlService.getTotalUnrealizedPnl(1L);

        assertEquals(0, new BigDecimal("300.00").compareTo(result));
    }

    // -------------------------------------------------------
    // 边界场景：用户无持仓（HoldingService 抛 BusinessException）→ 返回 ZERO
    // -------------------------------------------------------
    @Test
    void getTotalUnrealizedPnl_NoHoldings_ReturnsZero() {
        when(holdingService.getUserHoldingsWithprice(1L))
                .thenThrow(new BusinessException(CommonErrorCode.NOT_FOUND, "Holding not found"));

        BigDecimal result = unrealizedPnlService.getTotalUnrealizedPnl(1L);

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    // -------------------------------------------------------
    // 边界场景：某持仓 pl 为 null → 过滤掉，不影响总和
    // -------------------------------------------------------
    @Test
    void getTotalUnrealizedPnl_SomePlNull_NullsAreFiltered() {
        when(holdingService.getUserHoldingsWithprice(1L))
                .thenReturn(List.of(
                        makeDto("AAPL", new BigDecimal("100.00")),
                        makeDto("TSLA", null)          // pl 为 null
                ));

        BigDecimal result = unrealizedPnlService.getTotalUnrealizedPnl(1L);

        assertEquals(0, new BigDecimal("100.00").compareTo(result));
    }

    // -------------------------------------------------------
    // 边界场景：所有持仓均平手（pl = 0）
    // -------------------------------------------------------
    @Test
    void getTotalUnrealizedPnl_AllPlZero_ReturnsZero() {
        when(holdingService.getUserHoldingsWithprice(1L))
                .thenReturn(List.of(
                        makeDto("AAPL", BigDecimal.ZERO),
                        makeDto("TSLA", BigDecimal.ZERO)
                ));

        BigDecimal result = unrealizedPnlService.getTotalUnrealizedPnl(1L);

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }
}

