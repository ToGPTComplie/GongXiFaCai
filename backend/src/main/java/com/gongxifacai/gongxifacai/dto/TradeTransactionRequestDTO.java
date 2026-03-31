package com.gongxifacai.gongxifacai.dto;

import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeTransactionRequestDTO {

    @NotBlank(message = "资产代码不能为空")
    private String ticker;

    @NotNull(message = "资产类型不能为空")
    private Holding.AssetType assetType;

    @NotNull(message = "交易类型不能为空")
    private TradeTransaction.TransactionType transactionType;

    @NotNull(message = "交易数量不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "交易数量必须大于0")
    private BigDecimal quantity;

    @NotNull(message = "交易价格不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "交易价格必须大于0")
    private BigDecimal price;
}
