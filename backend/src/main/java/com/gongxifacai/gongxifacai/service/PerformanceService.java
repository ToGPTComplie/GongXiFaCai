package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.PerformancePeriod;
import com.gongxifacai.gongxifacai.dto.PerformanceSummaryDTO;
import com.gongxifacai.gongxifacai.dto.TopBottomClosedTradesDTO;

public interface PerformanceService {

    /**
     * 查询用户在指定时间段内的已实现盈亏汇总
     *
     * @param userId 用户 ID
     * @param period 时间段枚举（H24 / D7 / D30 / D90 / YTD / ALL）
     * @return 已实现盈亏汇总 DTO
     */
    PerformanceSummaryDTO getPerformanceSummary(Long userId, PerformancePeriod period);

    /**
     * 查询用户在指定时间段内，按标的聚合的已实现盈亏排行（Top 3 盈利 / Top 3 亏损）
     *
     * @param userId 用户 ID
     * @param period 时间段枚举（H24 / D7 / D30 / D90 / YTD / ALL）
     * @return Top Gainers / Top Losers DTO
     */
    TopBottomClosedTradesDTO getTopBottomClosedTrades(Long userId, PerformancePeriod period);
}



