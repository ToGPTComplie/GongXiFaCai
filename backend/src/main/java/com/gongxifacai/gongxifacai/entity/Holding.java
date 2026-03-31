package com.gongxifacai.gongxifacai.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "holding")
public class Holding extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    // 当前持仓数量
    @DecimalMin(value = "0.0", message = "持仓数量必须大于等于0")
    @Column(name = "quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal quantity;

    // 平均持仓成本 (区别于单笔交易的 price)
    @DecimalMin(value = "0.0", message = "平均成本必须大于等于0")
    @Column(name = "average_cost", precision = 19, scale = 4, nullable = false)
    private BigDecimal averageCost;

    public enum AssetType {
        STOCK, BOND
    }
}
