package com.gongxifacai.gongxifacai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 风险分析接口的完整响应 DTO
 * 包含：组合数据摘要（供前端展示）+ AI 生成的四维风险分析
 */
@Data
public class RiskAnalysisResultDTO {

    // ===== 组合数据摘要（前端可直接用于展示图表/数字）=====

    /** 总资产（持仓市值 + 现金） */
    private BigDecimal totalAssets;

    /** 现金金额 */
    private BigDecimal cashAmount;

    /** 现金占总资产比例（小数，如 0.07 表示 7%） */
    private BigDecimal cashPercentage;

    /** 各持仓详情（含占比） */
    private List<HoldingRiskDTO> holdings;

    /** 各资产类别占比（如 {"STOCK": 0.78, "BOND": 0.15}） */
    private Map<String, BigDecimal> assetTypeBreakdown;

    // ===== AI 风险分析结果 =====

    /** 整体风险等级：HIGH / MEDIUM / LOW */
    private String overallRiskLevel;

    /** 整体风险摘要 */
    private String overallSummary;

    /** 单一资产集中度风险 */
    private RiskDimensionDTO singleAssetConcentration;

    /** 资产类别集中度风险（股票/债券比例） */
    private RiskDimensionDTO assetTypeConcentration;

    /** 行业集中度风险 */
    private SectorConcentrationDTO sectorConcentration;

    /** 现金比例风险 */
    private RiskDimensionDTO cashRatio;
}

