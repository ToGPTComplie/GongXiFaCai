package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.service.UnrealizedPnlService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 浮盈亏服务临时占位实现
 * TODO: 由同事替换为真实实现（对接外部价格 API）
 */
@Service
public class UnrealizedPnlServiceStub implements UnrealizedPnlService {

    @Override
    public BigDecimal getTotalUnrealizedPnl(Long userId) {
        // 占位：同事完成外部价格 API 对接后，删除此类并提供真实实现
        return BigDecimal.ZERO;
    }
}

