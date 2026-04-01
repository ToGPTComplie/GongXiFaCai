package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

    Page<TradeTransaction> findByUser_Id(Long userId, Pageable pageable);

    /**
     * 全量 SUM：计算用户截至目前的累计已实现盈亏
     * 不依赖历史快照，任何时候跑都保证准确
     */
    @Query("SELECT COALESCE(SUM(t.realizedPnl), 0) FROM TradeTransaction t " +
           "WHERE t.user.id = :userId AND t.transactionType = :transactionType")
    BigDecimal sumRealizedPnlByUserId(
            @Param("userId") Long userId,
            @Param("transactionType") TradeTransaction.TransactionType transactionType);

    /**
     * 区间 SUM：计算用户在 startTime 之后的已实现盈亏
     * 用于 24h / 7d / 30d / 90d / YTD 等时间段统计
     */
    @Query("SELECT COALESCE(SUM(t.realizedPnl), 0) FROM TradeTransaction t " +
           "WHERE t.user.id = :userId AND t.transactionType = :transactionType AND t.createdAt >= :startTime")
    BigDecimal sumRealizedPnlByUserIdSince(
            @Param("userId") Long userId,
            @Param("transactionType") TradeTransaction.TransactionType transactionType,
            @Param("startTime") LocalDateTime startTime);

    /**
     * 获取用户所有指定类型的交易记录，按时间升序
     * 用于 ALL 时间段的指标计算
     */
    @Query("SELECT t FROM TradeTransaction t WHERE t.user.id = :userId " +
           "AND t.transactionType = :type ORDER BY t.createdAt ASC")
    List<TradeTransaction> findAllByUserAndType(
            @Param("userId") Long userId,
            @Param("type") TradeTransaction.TransactionType type);

    /**
     * 获取用户 startTime 之后指定类型的交易记录，按时间升序
     * 用于各时间段（H24/D7/D30/D90/YTD）的指标计算
     */
    @Query("SELECT t FROM TradeTransaction t WHERE t.user.id = :userId " +
           "AND t.transactionType = :type AND t.createdAt >= :startTime ORDER BY t.createdAt ASC")
    List<TradeTransaction> findByUserAndTypeSince(
            @Param("userId") Long userId,
            @Param("type") TradeTransaction.TransactionType type,
            @Param("startTime") LocalDateTime startTime);

    /**
     * 获取用户某 ticker 在 beforeTime 之前的所有指定类型交易，按时间升序
     * 用于 FIFO 平均持仓时间计算：取某 ticker 历史 BUY 队列
     */
    @Query("SELECT t FROM TradeTransaction t WHERE t.user.id = :userId " +
           "AND t.ticker = :ticker AND t.transactionType = :type " +
           "AND t.createdAt < :beforeTime ORDER BY t.createdAt ASC")
    List<TradeTransaction> findByUserAndTickerAndTypeBefore(
            @Param("userId") Long userId,
            @Param("ticker") String ticker,
            @Param("type") TradeTransaction.TransactionType type,
            @Param("beforeTime") LocalDateTime beforeTime);
}
