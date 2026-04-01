package com.gongxifacai.gongxifacai.dto;

import com.gongxifacai.gongxifacai.entity.TradeTransaction;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TradePlanDTO {
    private String ticker;
    private BigDecimal currentPercentage; // 当前占比
    private BigDecimal targetPercentage; // 目标占比
    private BigDecimal diffPercentage; // 占比差异 (+表示需买入，-表示需卖出)
    private BigDecimal tradeAmount; // 预计交易金额 (+表示需买入，-表示需卖出)
    private BigDecimal tradeQuantity;
}
