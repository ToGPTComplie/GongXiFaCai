package com.gongxifacai.gongxifacai.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "targetAllocation")
public class TargetAllocation extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type")
    private Holding.AssetType assetType;

    @Column(name = "target_percentage", nullable = false)
    @DecimalMin(value = "0.0",  message = "目标比例必须大于等于0")
    @DecimalMax(value = "1.0",  message = "目标比例必须小于等于1")
    private BigDecimal targetPercentage;
}
