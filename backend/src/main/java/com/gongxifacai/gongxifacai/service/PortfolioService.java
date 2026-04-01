package com.gongxifacai.gongxifacai.service;

import com.gongxifacai.gongxifacai.dto.TargetAllocationDTO;
import com.gongxifacai.gongxifacai.dto.TradePlanDTO;
import com.gongxifacai.gongxifacai.entity.TargetAllocation;

import java.util.List;

public interface PortfolioService {
    List<TargetAllocationDTO> getTargetAllocations(Long userId);

    /**
     * 设置用户的目标配置比例
     */
    void setTargetAllocations(Long userId, List<TargetAllocation> targetAllocationList);

    /**
     * 预览再平衡操作（只返回需要进行的买卖交易列表，不实际执行）
     */
    List<TradePlanDTO> previewRebalance(Long userId);

    /**
     * 实际执行再平衡
     */
    List<TradePlanDTO> executeRebalance(Long userId);

}
