package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.dto.KLineCandleDTO;
import com.gongxifacai.gongxifacai.entity.Holding;

import java.math.BigDecimal;
import java.util.List;

public interface HoldingService {
    List<Holding> getUserHoldings(Long userId);

    List<HoldingDTO> getUserHoldingsWithprice(Long userId);

    List<KLineCandleDTO> getKLineData(Long userId, String symbol);

    Holding getHolding(Long userId, String ticker);

    BigDecimal getMarketPrice(String ticker);

    Holding getOrCreateHolding(Long userId, String ticker, Holding.AssetType assetType);

    Holding applyBuy(Long userId, String ticker, Holding.AssetType assetType, BigDecimal quantity, BigDecimal totalAmount);

    Holding applySell(Long userId, String ticker, BigDecimal quantity);

    BigDecimal calculateRealizedPnl(Long userId, String ticker, BigDecimal quantity, BigDecimal price);
}
