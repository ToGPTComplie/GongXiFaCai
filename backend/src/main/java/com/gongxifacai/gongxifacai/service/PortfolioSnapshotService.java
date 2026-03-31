package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.PortfolioPerformanceDTO;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioSnapshotService {

    /**
     * 为指定用户生成（或更新）某天的盈亏快照
     * 由定时任务收盘后调用
     */
    void createOrUpdateSnapshot(Long userId, LocalDate date);

    /**
     * 查询用户在日期范围内的每日盈亏数据，供折线图使用
     */
    List<PortfolioPerformanceDTO> getPerformanceHistory(Long userId, LocalDate startDate, LocalDate endDate);
}

