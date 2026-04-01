package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.dto.HoldingRiskDTO;
import com.gongxifacai.gongxifacai.dto.RiskAnalysisResultDTO;
import com.gongxifacai.gongxifacai.dto.RiskDimensionDTO;
import com.gongxifacai.gongxifacai.dto.SectorConcentrationDTO;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.service.HoldingService;
import com.gongxifacai.gongxifacai.service.RiskAnalysisService;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.util.BigDecimalUtil;
import com.gongxifacai.gongxifacai.util.OpenAiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAnalysisServiceImpl implements RiskAnalysisService {

    private final HoldingService holdingService;
    private final UserService userService;
    private final OpenAiClient openAiClient;

    private static final Gson GSON = new Gson();

    /**
     * System Prompt：角色设定 + 严格的 JSON 返回格式定义
     */
    private static final String SYSTEM_PROMPT = """
            You are a professional investment portfolio risk analyst. \
            Analyze the portfolio data provided by the user and return ONLY valid JSON (no markdown, no extra text).
            
            The JSON must strictly follow this structure:
            {
              "overallRiskLevel": "HIGH|MEDIUM|LOW",
              "overallSummary": "overall risk summary in English",
              "singleAssetConcentration": {
                "riskLevel": "HIGH|MEDIUM|LOW",
                "issues": ["issue description in English"],
                "suggestion": "suggestion in English"
              },
              "assetTypeConcentration": {
                "riskLevel": "HIGH|MEDIUM|LOW",
                "issues": ["issue description in English"],
                "suggestion": "suggestion in English"
              },
              "sectorConcentration": {
                "riskLevel": "HIGH|MEDIUM|LOW",
                "sectors": { "SectorName": 0.45 },
                "issues": ["issue description in English"],
                "suggestion": "suggestion in English"
              },
              "cashRatio": {
                "riskLevel": "HIGH|MEDIUM|LOW",
                "issues": ["issue description in English"],
                "suggestion": "suggestion in English"
              }
            }
            
            Risk level criteria — evaluate each threshold exactly as defined, without adjustment:
            - singleAssetConcentration: HIGH if any single asset > 30%, MEDIUM if 20%-30%, LOW if all < 20%
            - assetTypeConcentration: HIGH if stocks > 80%, MEDIUM if stocks 60%-80%, LOW if stocks < 60%
            - sectorConcentration: HIGH if any single sector > 50%, MEDIUM if 30%-50%, LOW if all < 30%.
              Sector percentages must be calculated as a share of total portfolio assets (holdings + cash), not stock-only.
            - cashRatio: HIGH risk if cash < 3%, MEDIUM if cash 3%-8%, LOW if cash > 8%
            - overallRiskLevel: HIGH if any dimension is HIGH, MEDIUM if any is MEDIUM but none HIGH, otherwise LOW
            
            All text fields (overallSummary, issues, suggestion) must be written in English.
            The "sectors" values are the sector's percentage of total portfolio assets (decimal, e.g. 0.78).
            """;

    @Override
    public RiskAnalysisResultDTO analyzeRisk(Long userId) {
        // 1. 校验用户
        if (!userService.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.USER_NOT_FOUND);
        }

        // 2. 获取带实时市价的持仓列表
        List<HoldingDTO> holdingDTOs = holdingService.getUserHoldingsWithprice(userId);

        // 3. 获取可用现金
        BigDecimal cash = userService.getAvailableCash(userId);
        if (cash == null) {
            cash = BigDecimal.ZERO;
        }

        // 4. 计算总持仓市值
        BigDecimal totalHoldingsMarketValue = holdingDTOs.stream()
                .map(HoldingDTO::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. 计算总资产
        BigDecimal totalAssets = totalHoldingsMarketValue.add(cash);
        if (!BigDecimalUtil.isGreaterThanZero(totalAssets)) {
            throw new BusinessException("总资产为0，无法进行风险分析");
        }

        // 6. 构建 HoldingRiskDTO 列表（含每只持仓占比）
        final BigDecimal finalTotalAssets = totalAssets;
        List<HoldingRiskDTO> holdingRiskDTOs = holdingDTOs.stream().map(h -> {
            HoldingRiskDTO dto = new HoldingRiskDTO();
            dto.setTicker(h.getTicker());
            dto.setAssetType(h.getAssetType().name());
            dto.setMarketValue(h.getMarketValue());
            BigDecimal percentage = h.getMarketValue()
                    .divide(finalTotalAssets, 4, RoundingMode.HALF_UP);
            dto.setPercentage(percentage);
            return dto;
        }).toList();

        // 7. 计算各资产类别占比
        Map<String, BigDecimal> assetTypeBreakdown = holdingRiskDTOs.stream()
                .collect(Collectors.groupingBy(
                        HoldingRiskDTO::getAssetType,
                        Collectors.reducing(BigDecimal.ZERO, HoldingRiskDTO::getPercentage, BigDecimal::add)
                ));

        // 8. 计算现金占比
        BigDecimal cashPercentage = cash.divide(totalAssets, 4, RoundingMode.HALF_UP);

        // 9. 构建传给 AI 的 Prompt
        String userPrompt = buildUserPrompt(holdingRiskDTOs, assetTypeBreakdown, cash, cashPercentage, totalAssets);

        log.info("Sending risk analysis request to OpenAI for userId: {}", userId);

        // 10. 调用 OpenAI API
        String aiResponseText = openAiClient.chat(SYSTEM_PROMPT, userPrompt);

        // 11. 解析 AI 返回的 JSON
        JsonObject aiJson = JsonParser.parseString(aiResponseText).getAsJsonObject();

        // 12. 组装最终结果
        RiskAnalysisResultDTO result = new RiskAnalysisResultDTO();

        // 摘要数据
        result.setTotalAssets(totalAssets);
        result.setCashAmount(cash);
        result.setCashPercentage(cashPercentage);
        result.setHoldings(holdingRiskDTOs);
        result.setAssetTypeBreakdown(assetTypeBreakdown);

        // AI 分析结果
        result.setOverallRiskLevel(aiJson.get("overallRiskLevel").getAsString());
        result.setOverallSummary(aiJson.get("overallSummary").getAsString());
        result.setSingleAssetConcentration(
                GSON.fromJson(aiJson.get("singleAssetConcentration"), RiskDimensionDTO.class));
        result.setAssetTypeConcentration(
                GSON.fromJson(aiJson.get("assetTypeConcentration"), RiskDimensionDTO.class));
        result.setSectorConcentration(
                GSON.fromJson(aiJson.get("sectorConcentration"), SectorConcentrationDTO.class));
        result.setCashRatio(
                GSON.fromJson(aiJson.get("cashRatio"), RiskDimensionDTO.class));

        return result;
    }

    /**
     * 构建传给 OpenAI 的用户 Prompt，包含组合的所有数据
     */
    private String buildUserPrompt(List<HoldingRiskDTO> holdings,
                                   Map<String, BigDecimal> assetTypeBreakdown,
                                   BigDecimal cash,
                                   BigDecimal cashPercentage,
                                   BigDecimal totalAssets) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下投资组合的风险：\n\n");

        sb.append("=== 总资产 ===\n");
        sb.append(String.format("总资产：$%.2f\n\n", totalAssets));

        sb.append("=== 持仓明细 ===\n");
        for (HoldingRiskDTO h : holdings) {
            sb.append(String.format("- %s (%s)：市值 $%.2f，占比 %.2f%%\n",
                    h.getTicker(),
                    h.getAssetType(),
                    h.getMarketValue(),
                    h.getPercentage().multiply(BigDecimal.valueOf(100))));
        }
        sb.append("\n");

        sb.append("=== 资产类别分布 ===\n");
        assetTypeBreakdown.forEach((type, pct) ->
                sb.append(String.format("- %s：%.2f%%\n", type, pct.multiply(BigDecimal.valueOf(100)))));
        sb.append(String.format("- CASH：%.2f%%\n\n", cashPercentage.multiply(BigDecimal.valueOf(100))));

        sb.append("=== 现金 ===\n");
        sb.append(String.format("现金金额：$%.2f，占比 %.2f%%\n", cash, cashPercentage.multiply(BigDecimal.valueOf(100))));

        return sb.toString();
    }
}

