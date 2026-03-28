package com.gongxifacai.gongxifacai.exception;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void testConstructorWithErrorCode() {
        // 方式一：测试严格 Enum 构造
        BusinessException exception = new BusinessException(CommonErrorCode.NOT_FOUND);
        
        assertEquals(404, exception.getCode());
        assertEquals("请求的资源不存在", exception.getMessage());
    }

    @Test
    void testConstructorWithErrorCodeAndDynamicMessage() {
        // 方式二：测试 Enum + 动态消息
        String dynamicMessage = "未找到股票代码: AAPL";
        BusinessException exception = new BusinessException(CommonErrorCode.NOT_FOUND, dynamicMessage);
        
        assertEquals(404, exception.getCode());
        assertEquals(dynamicMessage, exception.getMessage());
    }

    @Test
    void testConstructorWithMessageOnly() {
        // 方式三：测试仅传字符串（新人兜底方式）
        String simpleMessage = "余额不足";
        BusinessException exception = new BusinessException(simpleMessage);
        
        // 应该自动绑定到 400
        assertEquals(400, exception.getCode());
        assertEquals(simpleMessage, exception.getMessage());
    }
}
