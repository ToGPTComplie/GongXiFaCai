package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.RiskAnalysisResultDTO;

public interface RiskAnalysisService {

    /**
     * 分析用户投资组合的风险
     * 涵盖：单一资产集中度、资产类别集中度、行业集中度、现金比例
     *
     * @param userId 用户 ID
     * @return 风险分析结果（含组合摘要 + AI 四维分析）
     */
    RiskAnalysisResultDTO analyzeRisk(Long userId);
}

