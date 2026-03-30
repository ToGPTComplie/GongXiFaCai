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
@Table(name = "trade_transaction")
public class TradeTransaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private Holding.AssetType assetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @DecimalMin(value = "0.0", message = "交易数量必须大于等于0")
    @Column(name = "quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "价格必须大于等于0")
    @Column(name = "price", precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "总金额必须大于等于0")
    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    public enum TransactionType {
        BUY, SELL
    }
}
