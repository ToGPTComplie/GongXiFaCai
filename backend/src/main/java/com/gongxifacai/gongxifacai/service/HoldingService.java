package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.HoldingDTO;
import com.gongxifacai.gongxifacai.dto.KLineCandleDTO;
import com.gongxifacai.gongxifacai.entity.Holding;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface HoldingService {
    List<Holding> getUserHoldings(Long userId);

    List<HoldingDTO> getUserHoldingsWithprice(Long userId);

    List<KLineCandleDTO> getKLineData(Long userId, String symbol);

    Holding getHolding(Long userId, String ticker);

    BigDecimal getMarketPrice(String ticker);

    Holding getOrCreateHolding(Long userId, String ticker, Holding.AssetType assetType);

    Map<Holding, BigDecimal> getMarketPricesByHolding(List<Holding> holdings);

    Map<String, BigDecimal> getMarketPricesByTicker(List<String> tickers);

    Holding applyBuy(Long userId, String ticker, Holding.AssetType assetType, BigDecimal quantity, BigDecimal totalAmount);

    Holding applySell(Long userId, String ticker, BigDecimal quantity);

    BigDecimal calculateHoldingMarketValue(Holding holding);

    BigDecimal calculateTotalHoldingsMarketValue(List<Holding> holdings);

    BigDecimal calculateTotalHoldingsMarketValue(Map<Holding, BigDecimal> currentMarketPriceMap);
    BigDecimal calculateRealizedPnl(Long userId, String ticker, BigDecimal quantity, BigDecimal price);
}
