package com.gongxifacai.gongxifacai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.WatchlistItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WatchlistItemResponseDTO {

    private Long id;
    private Long userId;
    private String ticker;
    private Holding.AssetType assetType;
    private String notes;
    private BigDecimal targetBuyPrice;
    private BigDecimal alertPriceHigh;
    private BigDecimal alertPriceLow;

    // 预留字段，当前阶段为 null，接入行情 API 后填充
    private BigDecimal currentPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    public static WatchlistItemResponseDTO from(WatchlistItem item) {
        WatchlistItemResponseDTO dto = new WatchlistItemResponseDTO();
        dto.setId(item.getId());
        dto.setUserId(item.getUser().getId());
        dto.setTicker(item.getTicker());
        dto.setAssetType(item.getAssetType());
        dto.setNotes(item.getNotes());
        dto.setTargetBuyPrice(item.getTargetBuyPrice());
        dto.setAlertPriceHigh(item.getAlertPriceHigh());
        dto.setAlertPriceLow(item.getAlertPriceLow());
        dto.setCurrentPrice(null); // 接入行情 API 后在 Service 层填充
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}

