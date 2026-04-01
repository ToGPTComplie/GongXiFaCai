package com.gongxifacai.gongxifacai.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 风险分析中每只持仓的摘要数据
 */
@Data
public class HoldingRiskDTO {
    private String ticker;
    private String assetType;
    private BigDecimal marketValue;
    // 该持仓市值占总资产的比例（小数，如 0.42 表示 42%）
    private BigDecimal percentage;
}

