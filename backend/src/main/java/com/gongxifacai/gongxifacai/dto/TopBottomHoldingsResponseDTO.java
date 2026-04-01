package com.gongxifacai.gongxifacai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TopBottomHoldingsResponseDTO {
    private List<HoldingDTO> topProfitable;
    private List<HoldingDTO> topLosing;
}

