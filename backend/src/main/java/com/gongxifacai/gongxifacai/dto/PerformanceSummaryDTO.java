package com.gongxifacai.gongxifacai.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 交易分析汇总结果：返回指定时间段内的各项表现指标
 */
@Data
public class PerformanceSummaryDTO {

    /** 查询的时间段 */
    private String period;

    /** 该时间段内的已实现盈亏总额（正数为盈利，负数为亏损） */
    private BigDecimal realizedPnl;

    /** 该时间段内的已平仓交易总数 */
    private Long totalTrades;

    /**
     * 胜率（0.0 ~ 1.0，如 0.65 表示 65%）
     * 无交易时为 null
     */
    private BigDecimal winRate;

    /**
     * Profit Factor = 总盈利 / 总亏损绝对值
     * 无亏损交易时为 null（分母为 0）
     * 无交易时为 null
     */
    private BigDecimal profitFactor;

    /**
     * 平均持仓天数（FIFO 加权平均）
     * 无交易时为 null
     */
    private BigDecimal avgHoldingDays;
}
