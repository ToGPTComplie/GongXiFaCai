package com.gongxifacai.gongxifacai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gongxifacai.gongxifacai.entity.PortfolioSnapshot;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 折线图数据点：横轴日期 + 纵轴总盈亏
 */
@Data
public class PortfolioPerformanceDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private BigDecimal realizedPnl;

    private BigDecimal unrealizedPnl;

    // 折线图纵轴
    private BigDecimal totalPnl;

    public static PortfolioPerformanceDTO from(PortfolioSnapshot snapshot) {
        PortfolioPerformanceDTO dto = new PortfolioPerformanceDTO();
        dto.setDate(snapshot.getSnapshotDate());
        dto.setRealizedPnl(snapshot.getRealizedPnl());
        dto.setUnrealizedPnl(snapshot.getUnrealizedPnl());
        dto.setTotalPnl(snapshot.getTotalPnl());
        return dto;
    }
}

