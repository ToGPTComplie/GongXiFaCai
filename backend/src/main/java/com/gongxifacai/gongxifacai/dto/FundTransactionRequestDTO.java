package com.gongxifacai.gongxifacai.dto;

import com.gongxifacai.gongxifacai.entity.FundTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundTransactionRequestDTO {

    @NotNull(message = "交易类型不能为空")
    private FundTransaction.FundTransactionType transactionType;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "金额必须大于0")
    private BigDecimal amount;

    private String description;
}
