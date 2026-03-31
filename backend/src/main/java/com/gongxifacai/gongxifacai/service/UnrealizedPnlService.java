package com.gongxifacai.gongxifacai.service;

import java.math.BigDecimal;

/**
 * 浮盈亏服务接口 —— 由同事基于外部价格 API 实现
 * 约定：返回该用户所有持仓的浮盈亏之和
 *       计算公式：SUM((当日收盘价 - holding.averageCost) × holding.quantity)
 */
public interface UnrealizedPnlService {

    /**
     * 获取用户当前所有持仓的总浮盈亏
     *
     * @param userId 用户 ID
     * @return 总浮盈亏（可为负数）
     */
    BigDecimal getTotalUnrealizedPnl(Long userId);
}

