package com.gongxifacai.gongxifacai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 按标的聚合的已实现盈亏摘要
 */
@Data
@AllArgsConstructor
public class TickerPnlDTO {

    /** 股票代码 */
    private String ticker;

    /** 该 ticker 在时间段内的已实现盈亏合计 */
    private BigDecimal totalRealizedPnl;

    /** 该 ticker 在时间段内的平仓笔数 */
    private int tradeCount;
}

