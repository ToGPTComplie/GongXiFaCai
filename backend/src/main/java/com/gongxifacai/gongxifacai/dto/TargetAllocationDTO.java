package com.gongxifacai.gongxifacai.dto;

import com.gongxifacai.gongxifacai.entity.TargetAllocation;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TargetAllocationDTO {

    private String ticker;

    private BigDecimal targetPercentage;

    public TargetAllocation toTargetAllocation(){
        TargetAllocation targetAllocation = new TargetAllocation();
        targetAllocation.setTicker(ticker);
        targetAllocation.setTargetPercentage(targetPercentage);
        return targetAllocation;
    }
}
