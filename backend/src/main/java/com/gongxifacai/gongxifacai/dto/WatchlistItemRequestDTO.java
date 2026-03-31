package com.gongxifacai.gongxifacai.dto;

import com.gongxifacai.gongxifacai.entity.Holding;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WatchlistItemRequestDTO {

    // 创建时必填，更新时忽略（ticker/assetType 创建后不可修改）
    @NotBlank(message = "Ticker must not be blank")
    private String ticker;

    @NotNull(message = "Asset type must not be null")
    private Holding.AssetType assetType;

    // 以下字段创建和更新都可传
    private String notes;
    private BigDecimal targetBuyPrice;
    private BigDecimal alertPriceHigh;
    private BigDecimal alertPriceLow;
}

