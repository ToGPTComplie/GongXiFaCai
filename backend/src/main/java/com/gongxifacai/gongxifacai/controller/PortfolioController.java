package com.gongxifacai.gongxifacai.controller;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.PortfolioPerformanceDTO;
import com.gongxifacai.gongxifacai.service.PortfolioSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioSnapshotService portfolioSnapshotService;

    /**
     * 查询用户 Portfolio 折线图数据
     * GET /api/v1/users/{id}/portfolio/performance?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/{id}/portfolio/performance")
    public Result<List<PortfolioPerformanceDTO>> getPortfolioPerformance(
            @PathVariable("id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(portfolioSnapshotService.getPerformanceHistory(userId, startDate, endDate));
    }
}



