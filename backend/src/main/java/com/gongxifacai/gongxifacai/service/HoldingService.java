package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.entity.Holding;

import java.math.BigDecimal;
import java.util.List;

public interface HoldingService {
    List<Holding> getUserHoldings(Long userId);

    Holding getHolding(Long userId, String ticker);

    Holding getOrCreateHolding(Long userId, String ticker, Holding.AssetType assetType);

    Holding applyBuy(Long userId, String ticker, Holding.AssetType assetType, BigDecimal quantity, BigDecimal totalAmount);

    Holding applySell(Long userId, String ticker, BigDecimal quantity);
}