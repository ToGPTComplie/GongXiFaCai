package com.gongxifacai.gongxifacai.dto;
import com.gongxifacai.gongxifacai.entity.Holding;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class HoldingDTO {
    private Long id;
    private String ticker;
    private Holding.AssetType assetType;
    private BigDecimal quantity;
    private BigDecimal averageCost;

    // 计算出来的字段（不存库）
    private BigDecimal marketValue;
    private BigDecimal pl;
}
