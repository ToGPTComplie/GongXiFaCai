package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.TargetAllocationDTO;
import com.gongxifacai.gongxifacai.dto.TradePlanDTO;
import com.gongxifacai.gongxifacai.entity.TargetAllocation;
import com.gongxifacai.gongxifacai.service.PortfolioService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping("/{id}/portfolio/target")
    public Result<Void> setTargetAllocations(
            @PathVariable("id") Long userId,
            @Valid @RequestBody List<TargetAllocationDTO> targetAllocationDTOs) {
        List<TargetAllocation> targetAllocations = targetAllocationDTOs.stream().map(TargetAllocationDTO::toTargetAllocation).toList();
        portfolioService.setTargetAllocations(userId, targetAllocations);
        return Result.success(null);
    }

    @GetMapping("/{id}/portfolio/rebalance-preview")
    public Result<List<TradePlanDTO>> previewRebalance(@PathVariable("id") Long userId) {
        return Result.success(portfolioService.previewRebalance(userId));
    }

}
