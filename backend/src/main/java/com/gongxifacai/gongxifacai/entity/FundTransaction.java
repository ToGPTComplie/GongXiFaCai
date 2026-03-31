package com.gongxifacai.gongxifacai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "fund_transaction")
public class FundTransaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private FundTransactionType transactionType;

    @DecimalMin(value = "0.0", message = "总金额必须大于等于0")
    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "description")
    private String description; // 比如：银行卡转入、提现到银行卡等描述

    public enum FundTransactionType {
        DEPOSIT, WITHDRAW
    }
}
