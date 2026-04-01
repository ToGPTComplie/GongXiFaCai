package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.service.HoldingService;
import com.gongxifacai.gongxifacai.service.UnrealizedPnlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 浮盈亏服务真实实现
 * 对该用户所有持仓调用 Yahoo Finance 获取实时价格，
 * 计算并返回总浮盈亏：SUM((marketPrice - averageCost) × quantity)
 */
@Service
@RequiredArgsConstructor
public class UnrealizedPnlServiceImpl implements UnrealizedPnlService {

    private final HoldingService holdingService;

    @Override
    public BigDecimal getTotalUnrealizedPnl(Long userId) {
        List<HoldingDTO> holdings;
        try {
            holdings = holdingService.getUserHoldingsWithprice(userId);
        } catch (BusinessException ex) {
            // 用户暂无持仓，浮盈亏为 0
            return BigDecimal.ZERO;
        }
        return holdings.stream()
                .map(HoldingDTO::getPl)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

