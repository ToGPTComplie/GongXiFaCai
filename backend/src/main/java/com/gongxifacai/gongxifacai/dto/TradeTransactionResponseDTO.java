package com.gongxifacai.gongxifacai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TradeTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeTransactionResponseDTO {

    private Long id;
    private Long userId;
    private String ticker;
    private Holding.AssetType assetType;
    private TradeTransaction.TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    // SELL 时有值，BUY 时为 null
    private BigDecimal realizedPnl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    public static TradeTransactionResponseDTO from(TradeTransaction t) {
        TradeTransactionResponseDTO dto = new TradeTransactionResponseDTO();
        dto.setId(t.getId());
        dto.setUserId(t.getUser().getId());
        dto.setTicker(t.getTicker());
        dto.setAssetType(t.getAssetType());
        dto.setTransactionType(t.getTransactionType());
        dto.setQuantity(t.getQuantity());
        dto.setPrice(t.getPrice());
        dto.setTotalAmount(t.getTotalAmount());
        dto.setRealizedPnl(t.getRealizedPnl());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }
}

