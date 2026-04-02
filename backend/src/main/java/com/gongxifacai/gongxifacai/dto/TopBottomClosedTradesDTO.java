package com.gongxifacai.gongxifacai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 已平仓交易的 Top Gainers / Top Losers 响应体
 */
@Data
@AllArgsConstructor
public class TopBottomClosedTradesDTO {

    /** 已实现盈亏最高的前 3 个标的 */
    private List<TickerPnlDTO> topGainers;

    /** 已实现亏损最深的前 3 个标的 */
    private List<TickerPnlDTO> topLosers;
}

