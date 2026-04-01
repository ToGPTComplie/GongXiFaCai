package com.gongxifacai.gongxifacai.dto;

import lombok.Data;

import java.util.List;

/**
 * 单个风险维度的分析结果（用于：单一资产集中度、资产类别集中度、现金比例）
 */
@Data
public class RiskDimensionDTO {
    // HIGH / MEDIUM / LOW
    private String riskLevel;
    private List<String> issues;
    private String suggestion;
}

