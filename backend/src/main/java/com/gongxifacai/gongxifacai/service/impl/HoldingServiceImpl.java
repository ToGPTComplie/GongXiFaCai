package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.repository.HoldingRepository;
import com.gongxifacai.gongxifacai.service.HoldingService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.util.BigDecimalUtil;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final UserService userService;

    @Override
    public List<Holding> getUserHoldings(Long userId) {
        return holdingRepository.findByUserId(userId);
    }

    @Override
    public Holding getHolding(Long userId, String ticker) {
        List<Holding> holdings = getUserHoldings(userId);

        for (Holding holding : holdings) {
            if (holding.getTicker().equals(ticker)) {
                return holding;
            }
        }
        throw new BusinessException(CommonErrorCode.NOT_FOUND, "持仓不存在");
    }

    @Override
    public BigDecimal getMarketPrice(String ticker) {
        return null;
    }

    @Override
    @Transactional
    public Holding getOrCreateHolding(Long userId, String ticker, Holding.AssetType assetType) {
        List<Holding> holdings = getUserHoldings(userId);

        for (Holding holding : holdings) {
            if (holding.getTicker().equals(ticker)) {
                return holding;
            }
        }

        Holding holding = new Holding();

        holding.setUser(userService.getReferenceById(userId));
        holding.setTicker(ticker);
        holding.setAssetType(assetType);
        holding.setQuantity(BigDecimal.ZERO);
        holding.setAverageCost(BigDecimal.ZERO);
        return holdingRepository.save(holding);
    }

    @Override
    public Map<Holding, BigDecimal> getMarketPricesByHolding(List<Holding> holdings) {
        return holdings.stream().
                collect(Collectors.toMap(h -> h, h -> this.getMarketPrice(h.getTicker())));
    }

    @Override
    public Map<String, BigDecimal> getMarketPricesByTicker(List<String> tickers) {
        return tickers.stream().
                collect(Collectors.toMap(t -> t, this::getMarketPrice));
    }

    @Override
    @Transactional
    public Holding applyBuy(Long userId, String ticker, Holding.AssetType assetType, BigDecimal quantity, BigDecimal totalAmount) {
        Holding holding = getOrCreateHolding(userId, ticker, assetType);

        //计算平均持仓成本
        BigDecimal oldQuantity = holding.getQuantity();
        BigDecimal newQuantity = oldQuantity.add(quantity);
        BigDecimal newTotalAmount = holding.getAverageCost().multiply(oldQuantity).add(totalAmount);
        BigDecimal newAverageCost = newTotalAmount.divide(newQuantity, 4, RoundingMode.HALF_UP);

        holding.setQuantity(newQuantity);
        holding.setAverageCost(newAverageCost);

        return holdingRepository.save(holding);
    }

    @Override
    @Transactional
    public Holding applySell(Long userId, String ticker, BigDecimal quantity) {

        Holding holding = getHolding(userId, ticker);

        BigDecimal oldQuantity = holding.getQuantity();
        BigDecimal newQuantity = oldQuantity.subtract(quantity);

        // 持仓不足
        if (BigDecimalUtil.isLessThanZero(newQuantity)) {
            throw new BusinessException("持仓不足");
        }

        holding.setQuantity(newQuantity);
        return holdingRepository.save(holding);
    }

    @Override
    public BigDecimal calculateHoldingMarketValue(Holding holding) {
        return getMarketPrice(holding.getTicker()).multiply(holding.getQuantity());
    }

    @Override
    public BigDecimal calculateTotalHoldingsMarketValue(List<Holding> holdings) {
        return holdings.stream().map(this::calculateHoldingMarketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateTotalHoldingsMarketValue(Map<Holding, BigDecimal> currentMarketPriceMap) {
        return currentMarketPriceMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}
