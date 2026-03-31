package com.gongxifacai.gongxifacai.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "watchlist_item")
public class WatchlistItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private Holding.AssetType assetType;

    // 用户备注，如"等待回调买入"
    @Column(name = "notes")
    private String notes;

    // 目标买入价（可选）
    @Column(name = "target_buy_price", precision = 19, scale = 4)
    private BigDecimal targetBuyPrice;

    // 价格上限提醒（可选）
    @Column(name = "alert_price_high", precision = 19, scale = 4)
    private BigDecimal alertPriceHigh;

    // 价格下限提醒（可选）
    @Column(name = "alert_price_low", precision = 19, scale = 4)
    private BigDecimal alertPriceLow;
}

