package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.repository.HoldingRepository;
import com.gongxifacai.gongxifacai.repository.UserRepository;
import com.gongxifacai.gongxifacai.service.HoldingService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;

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
    @Transactional
    public Holding getOrCreateHolding(Long userId, String ticker, Holding.AssetType assetType) {
        List<Holding> holdings = getUserHoldings(userId);

        for (Holding holding : holdings) {
            if (holding.getTicker().equals(ticker)) {
                return holding;
            }
        }

        Holding holding = new Holding();
        
        holding.setUser(userRepository.getReferenceById(userId));
        holding.setTicker(ticker);
        holding.setAssetType(assetType);
        holding.setQuantity(BigDecimal.ZERO);
        holding.setAverageCost(BigDecimal.ZERO);
        return holdingRepository.save(holding);
    }
}
