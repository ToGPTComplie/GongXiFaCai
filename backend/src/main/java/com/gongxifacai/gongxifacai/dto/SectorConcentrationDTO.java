package com.gongxifacai.gongxifacai.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 行业集中度风险分析结果（在 RiskDimensionDTO 基础上增加行业分布 map）
 */
@Data
public class SectorConcentrationDTO {
    // HIGH / MEDIUM / LOW
    private String riskLevel;
    // 行业名称 -> 占总资产的比例（如 {"Technology": 0.78}）
    private Map<String, Double> sectors;
    private List<String> issues;
    private String suggestion;
}

