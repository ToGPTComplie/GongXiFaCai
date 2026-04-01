package com.gongxifacai.gongxifacai.repository;

import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

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
}
