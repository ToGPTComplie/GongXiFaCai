package com.gongxifacai.gongxifacai.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 每日 Portfolio 盈亏快照
 * 由定时任务在收盘后写入，每个用户每天一条（唯一约束：user_id + snapshot_date）
 */
@Getter
@Setter
@Entity
@Table(
    name = "portfolio_snapshot",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "snapshot_date"})
)
public class PortfolioSnapshot extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 快照日期（美东时间收盘日）
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    // 截至当天的累计已实现盈亏（全量 SUM TradeTransaction.realizedPnl）
    @Column(name = "realized_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal realizedPnl;

    // 当日收盘时的浮盈亏（由同事的服务计算）
    @Column(name = "unrealized_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal unrealizedPnl;

    // 折线图纵轴 = realized_pnl + unrealized_pnl
    @Column(name = "total_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalPnl;
}

