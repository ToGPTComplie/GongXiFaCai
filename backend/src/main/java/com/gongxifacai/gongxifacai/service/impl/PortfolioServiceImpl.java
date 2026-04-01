package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.dto.TradePlanDTO;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TargetAllocation;
import com.gongxifacai.gongxifacai.entity.User;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.TargetAllocationRepository;
import com.gongxifacai.gongxifacai.service.HoldingService;
import com.gongxifacai.gongxifacai.service.PortfolioService;
import com.gongxifacai.gongxifacai.service.UserService;
import com.gongxifacai.gongxifacai.util.BigDecimalUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final HoldingService holdingService;
    private final TargetAllocationRepository targetAllocationRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void setTargetAllocations(Long userId, List<TargetAllocation> targetAllocationList) {

        BigDecimal cashPercent = new BigDecimal("1.0000");
        User user = userService.getReferenceById(userId);

        for (TargetAllocation targetAllocation : targetAllocationList) {
            cashPercent = cashPercent.subtract(targetAllocation.getTargetPercentage());
            targetAllocation.setUser(user);
        }

        if (BigDecimalUtil.isLessThanZero(cashPercent)) {
            throw new BusinessException("总额度超过100%");
        }

        targetAllocationRepository.deleteAllByUser_Id(userId);

        targetAllocationRepository.saveAll(targetAllocationList);

    }

    @Override
    public List<TradePlanDTO> previewRebalance(Long userId) {

        List<Holding> holdings = holdingService.getUserHoldings(userId);

        List<TargetAllocation> targetAllocations = targetAllocationRepository.findByUserId(userId);

        Map<String, Holding> holdingMap = holdings.stream().
                collect(Collectors.toMap(Holding::getTicker, h -> h));

        Map<String, TargetAllocation> targetAllocationMap = targetAllocations.stream()
                .collect(Collectors.toMap(TargetAllocation::getTicker, t -> t));

        Set<String> allTickers = new HashSet<>();
        allTickers.addAll(holdingMap.keySet());
        allTickers.addAll(targetAllocationMap.keySet());

        Map<String, BigDecimal> marketPricesByTicker = holdingService.getMarketPricesByTicker(new ArrayList<>(allTickers));

        BigDecimal currentTotalAsset = userService.getAvailableCash(userId);
        if (currentTotalAsset == null) {
            currentTotalAsset = BigDecimal.ZERO;
        }

        for (Holding holding : holdings) {
            BigDecimal marketPrice = marketPricesByTicker.get(holding.getTicker());
            if (marketPrice != null && holding.getQuantity() != null) {
                currentTotalAsset = currentTotalAsset.add(marketPrice.multiply(holding.getQuantity()));
            }
        }

        List<TradePlanDTO> tradePlanDTOs = new ArrayList<>();

        if (currentTotalAsset.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("当前总资产不合法");
        }

        for (String ticker : allTickers) {

            BigDecimal currentMarketPrice = marketPricesByTicker.get(ticker);
            if (currentMarketPrice == null || currentMarketPrice.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Holding currentHolding = holdingMap.get(ticker);
            BigDecimal currentQuantity, currentAmount, currentPercentage;

            if (currentHolding == null || currentHolding.getQuantity() == null) {

                currentQuantity = BigDecimal.ZERO;
                currentAmount = BigDecimal.ZERO;
                currentPercentage = BigDecimal.ZERO;

            } else {

                currentQuantity = currentHolding.getQuantity();
                currentAmount = currentQuantity.multiply(currentMarketPrice);
                currentPercentage = currentAmount.divide(currentTotalAsset, 4, RoundingMode.HALF_UP);

            }

            TargetAllocation targetAllocation = targetAllocationMap.get(ticker);

            BigDecimal targetPercentage = (targetAllocation != null && targetAllocation.getTargetPercentage() != null)
                    ? targetAllocation.getTargetPercentage() : BigDecimal.ZERO;
            BigDecimal targetAmount = targetPercentage.multiply(currentTotalAsset);

            BigDecimal diffPercentage = targetPercentage.subtract(currentPercentage);

            if (diffPercentage.abs().compareTo(new BigDecimal("0.05")) < 0) {
                continue;
            }

            BigDecimal diffAmount = targetAmount.subtract(currentAmount);
            BigDecimal diffQuantity = diffAmount.divide(currentMarketPrice, 4, RoundingMode.HALF_UP);

            TradePlanDTO tradePlanDTO = new TradePlanDTO();
            tradePlanDTO.setTicker(ticker);
            tradePlanDTO.setTradeQuantity(diffQuantity);
            tradePlanDTO.setTradeAmount(diffAmount);
            tradePlanDTO.setCurrentPercentage(currentPercentage);
            tradePlanDTO.setTargetPercentage(targetPercentage);
            tradePlanDTO.setDiffPercentage(diffPercentage);

            tradePlanDTOs.add(tradePlanDTO);
        }
        return tradePlanDTOs;
    }

    @Override
    public void executeRebalance(Long userId) {

    }
}
