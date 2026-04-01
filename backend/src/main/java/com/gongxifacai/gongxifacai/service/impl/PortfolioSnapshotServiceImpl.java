package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.PortfolioPerformanceDTO;
import com.gongxifacai.gongxifacai.entity.PortfolioSnapshot;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.PortfolioSnapshotRepository;
import com.gongxifacai.gongxifacai.repository.TradeTransactionRepository;
import com.gongxifacai.gongxifacai.service.PortfolioSnapshotService;
import com.gongxifacai.gongxifacai.service.UnrealizedPnlService;
import com.gongxifacai.gongxifacai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioSnapshotServiceImpl implements PortfolioSnapshotService {

    private final PortfolioSnapshotRepository portfolioSnapshotRepository;
    private final TradeTransactionRepository tradeTransactionRepository;
    private final UnrealizedPnlService unrealizedPnlService;
    private final UserService userService;

    @Override
    @Transactional
    public void createOrUpdateSnapshot(Long userId, LocalDate date) {
        // 1. 全量 SUM：累计已实现盈亏（不依赖历史快照，随时跑随时准）
        BigDecimal realizedPnl = tradeTransactionRepository
                .sumRealizedPnlByUserId(userId, TradeTransaction.TransactionType.SELL);

        // 2. 当日浮盈亏（由同事的服务计算，使用收盘价）
        BigDecimal unrealizedPnl = unrealizedPnlService.getTotalUnrealizedPnl(userId);

        // 3. 折线图纵轴
        BigDecimal totalPnl = realizedPnl.add(unrealizedPnl);

        // 4. upsert：当天已有快照则更新，否则新建
        PortfolioSnapshot snapshot = portfolioSnapshotRepository
                .findByUser_IdAndSnapshotDate(userId, date)
                .orElse(new PortfolioSnapshot());

        if (snapshot.getId() == null) {
            snapshot.setUser(userService.getReferenceById(userId));
            snapshot.setSnapshotDate(date);
        }

        snapshot.setRealizedPnl(realizedPnl);
        snapshot.setUnrealizedPnl(unrealizedPnl);
        snapshot.setTotalPnl(totalPnl);

        portfolioSnapshotRepository.save(snapshot);
    }

    @Override
    public List<PortfolioPerformanceDTO> getPerformanceHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        if (!userService.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.USER_NOT_FOUND);
        }
        return portfolioSnapshotRepository
                .findByUser_IdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, startDate, endDate)
                .stream()
                .map(PortfolioPerformanceDTO::from)
                .collect(Collectors.toList());
    }
}

