package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.dto.PerformancePeriod;
import com.gongxifacai.gongxifacai.dto.PerformanceSummaryDTO;
import com.gongxifacai.gongxifacai.dto.TickerPnlDTO;
import com.gongxifacai.gongxifacai.dto.TopBottomClosedTradesDTO;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import com.gongxifacai.gongxifacai.exception.BusinessException;
import com.gongxifacai.gongxifacai.repository.TradeTransactionRepository;
import com.gongxifacai.gongxifacai.service.PerformanceService;
import com.gongxifacai.gongxifacai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final TradeTransactionRepository tradeTransactionRepository;
    private final UserService userService;

    @Override
    public PerformanceSummaryDTO getPerformanceSummary(Long userId, PerformancePeriod period) {
        if (!userService.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.USER_NOT_FOUND);
        }

        // 1. 根据时间段加载 SELL 记录（已按 createdAt ASC 排序）
        List<TradeTransaction> sells = loadSells(userId, period);

        // 2. 计算各项指标
        PerformanceSummaryDTO dto = new PerformanceSummaryDTO();
        dto.setPeriod(period.name());
        dto.setRealizedPnl(calcRealizedPnl(sells));
        dto.setTotalTrades((long) sells.size());
        dto.setWinRate(calcWinRate(sells));
        dto.setProfitFactor(calcProfitFactor(sells));
        dto.setAvgHoldingDays(calcAvgHoldingDays(userId, sells));
        return dto;
    }

    @Override
    public TopBottomClosedTradesDTO getTopBottomClosedTrades(Long userId, PerformancePeriod period) {
        if (!userService.existsById(userId)) {
            throw new BusinessException(CommonErrorCode.USER_NOT_FOUND);
        }

        List<TradeTransaction> sells = loadSells(userId, period);

        // 按 ticker 聚合：SUM(realizedPnl) 和笔数
        Map<String, List<TradeTransaction>> byTicker = sells.stream()
                .collect(Collectors.groupingBy(TradeTransaction::getTicker));

        List<TickerPnlDTO> aggregated = byTicker.entrySet().stream()
                .map(e -> {
                    BigDecimal total = e.getValue().stream()
                            .map(TradeTransaction::getRealizedPnl)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new TickerPnlDTO(e.getKey(), total, e.getValue().size());
                })
                .collect(Collectors.toList());

        // Top 3 盈利：降序取前 3
        List<TickerPnlDTO> topGainers = aggregated.stream()
                .sorted(Comparator.comparing(TickerPnlDTO::getTotalRealizedPnl).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // Top 3 亏损：升序取前 3
        List<TickerPnlDTO> topLosers = aggregated.stream()
                .sorted(Comparator.comparing(TickerPnlDTO::getTotalRealizedPnl))
                .limit(3)
                .collect(Collectors.toList());

        return new TopBottomClosedTradesDTO(topGainers, topLosers);
    }

    // ───────────────── 私有辅助方法 ─────────────────

    /** 根据时间段返回起始时间，ALL 返回 null */
    private LocalDateTime resolveStartTime(PerformancePeriod period) {
        return switch (period) {
            case ALL  -> null;
            case YTD  -> LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0, 0);
            case H24  -> LocalDateTime.now().minusHours(24);
            case D7   -> LocalDateTime.now().minusDays(7);
            case D30  -> LocalDateTime.now().minusDays(30);
            case D90  -> LocalDateTime.now().minusDays(90);
        };
    }

    /** 加载指定时间段内的 SELL 记录 */
    private List<TradeTransaction> loadSells(Long userId, PerformancePeriod period) {
        LocalDateTime startTime = resolveStartTime(period);
        if (startTime == null) {
            return tradeTransactionRepository.findAllByUserAndType(
                    userId, TradeTransaction.TransactionType.SELL);
        }
        return tradeTransactionRepository.findByUserAndTypeSince(
                userId, TradeTransaction.TransactionType.SELL, startTime);
    }

    /** 已实现盈亏 = SUM(realizedPnl) */
    private BigDecimal calcRealizedPnl(List<TradeTransaction> sells) {
        return sells.stream()
                .map(TradeTransaction::getRealizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 胜率 = 盈利笔数 / 总笔数
     * 无交易返回 null
     */
    private BigDecimal calcWinRate(List<TradeTransaction> sells) {
        if (sells.isEmpty()) return null;
        long winCount = sells.stream()
                .filter(t -> t.getRealizedPnl().compareTo(BigDecimal.ZERO) > 0)
                .count();
        return BigDecimal.valueOf(winCount)
                .divide(BigDecimal.valueOf(sells.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Profit Factor = 总盈利 / |总亏损|
     * 无亏损交易（分母为 0）或无交易时返回 null
     */
    private BigDecimal calcProfitFactor(List<TradeTransaction> sells) {
        if (sells.isEmpty()) return null;
        BigDecimal grossProfit = sells.stream()
                .map(TradeTransaction::getRealizedPnl)
                .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grossLoss = sells.stream()
                .map(TradeTransaction::getRealizedPnl)
                .filter(p -> p.compareTo(BigDecimal.ZERO) < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (grossLoss.compareTo(BigDecimal.ZERO) == 0) return null;
        return grossProfit.divide(grossLoss, 4, RoundingMode.HALF_UP);
    }

    /**
     * 平均持仓天数（FIFO 数量加权平均）
     *
     * 算法：
     * 1. 对每个 ticker 预加载其历史 BUY 队列（按时间 ASC）
     * 2. 按时间顺序处理每笔 SELL，FIFO 消费最早的 BUY lots
     * 3. 持仓天数 = SELL.createdAt - BUY.createdAt，按匹配数量加权
     * 4. 总加权天数 / 总匹配数量 = 平均持仓天数
     *
     * 安全保证：只消费严格早于当前 SELL 时刻的 BUY lot（防止跨时间线错配）
     */
    private BigDecimal calcAvgHoldingDays(Long userId, List<TradeTransaction> sells) {
        if (sells.isEmpty()) return null;

        // 预加载每个 ticker 的 BUY 队列
        // key: ticker, value: FIFO 队列（剩余可用 BUY lots）
        Map<String, LinkedList<BuyLot>> buyQueues = new HashMap<>();

        Set<String> tickers = sells.stream()
                .map(TradeTransaction::getTicker)
                .collect(Collectors.toSet());

        for (String ticker : tickers) {
            // 取该 ticker 在最后一笔 SELL 之前的所有 BUY（beforeTime 不含等号，加 1ns 兼容边界）
            LocalDateTime lastSellTime = sells.stream()
                    .filter(s -> ticker.equals(s.getTicker()))
                    .map(TradeTransaction::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElseThrow();

            List<TradeTransaction> buys = tradeTransactionRepository.findByUserAndTickerAndTypeBefore(
                    userId, ticker, TradeTransaction.TransactionType.BUY,
                    lastSellTime.plusNanos(1));

            LinkedList<BuyLot> queue = buys.stream()
                    .map(b -> new BuyLot(b.getQuantity(), b.getCreatedAt()))
                    .collect(Collectors.toCollection(LinkedList::new));

            buyQueues.put(ticker, queue);
        }

        BigDecimal totalWeightedDays = BigDecimal.ZERO;
        BigDecimal totalMatchedQty   = BigDecimal.ZERO;

        // 按 createdAt ASC 顺序逐笔 SELL 进行 FIFO 匹配
        for (TradeTransaction sell : sells) {
            LinkedList<BuyLot> queue = buyQueues.get(sell.getTicker());
            if (queue == null || queue.isEmpty()) continue;

            BigDecimal remainingToMatch = sell.getQuantity();

            while (remainingToMatch.compareTo(BigDecimal.ZERO) > 0 && !queue.isEmpty()) {
                BuyLot lot = queue.peek();

                // 安全校验：只消费严格早于本次 SELL 的 BUY
                if (!lot.buyTime.isBefore(sell.getCreatedAt())) break;

                BigDecimal matchedQty = remainingToMatch.min(lot.remainingQty);
                long days = ChronoUnit.DAYS.between(lot.buyTime, sell.getCreatedAt());

                totalWeightedDays = totalWeightedDays.add(
                        BigDecimal.valueOf(days).multiply(matchedQty));
                totalMatchedQty = totalMatchedQty.add(matchedQty);

                lot.remainingQty = lot.remainingQty.subtract(matchedQty);
                remainingToMatch = remainingToMatch.subtract(matchedQty);

                if (lot.remainingQty.compareTo(BigDecimal.ZERO) == 0) {
                    queue.poll(); // 该 lot 已完全消耗，移出队列
                }
            }
        }

        if (totalMatchedQty.compareTo(BigDecimal.ZERO) == 0) return null;
        return totalWeightedDays.divide(totalMatchedQty, 2, RoundingMode.HALF_UP);
    }

    /** FIFO 匹配辅助类：表示一批 BUY 记录的剩余可用数量 */
    private static class BuyLot {
        BigDecimal remainingQty;
        final LocalDateTime buyTime;

        BuyLot(BigDecimal qty, LocalDateTime time) {
            this.remainingQty = qty;
            this.buyTime = time;
        }
    }
}
