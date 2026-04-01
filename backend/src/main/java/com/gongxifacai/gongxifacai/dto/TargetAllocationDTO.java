package com.gongxifacai.gongxifacai.dto;

import com.gongxifacai.gongxifacai.entity.Holding;
import com.gongxifacai.gongxifacai.entity.TargetAllocation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TargetAllocationDTO {

    @NotBlank(message = "资产代码不能为空")
    private String ticker;

    @NotNull(message = "资产类型不能为空")
    private Holding.AssetType assetType;

    @NotNull(message = "目标比例不能为空")
    @DecimalMin(value = "0.0", message = "目标比例必须大于等于0")
    @DecimalMax(value = "1.0", message = "目标比例必须小于等于1")
    private BigDecimal targetPercentage;

    public TargetAllocation toTargetAllocation(){
        TargetAllocation targetAllocation = new TargetAllocation();
        targetAllocation.setTicker(ticker);
        targetAllocation.setAssetType(assetType);
        targetAllocation.setTargetPercentage(targetPercentage);
        return targetAllocation;
    }

    public static TargetAllocationDTO fromEntity(TargetAllocation targetAllocation) {
        TargetAllocationDTO dto = new TargetAllocationDTO();
        dto.setTicker(targetAllocation.getTicker());
        dto.setAssetType(targetAllocation.getAssetType());
        dto.setTargetPercentage(targetAllocation.getTargetPercentage());
        return dto;
    }
}
