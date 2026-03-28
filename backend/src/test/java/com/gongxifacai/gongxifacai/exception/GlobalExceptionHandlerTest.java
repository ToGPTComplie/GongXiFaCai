package com.gongxifacai.gongxifacai.exception;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.common.Result;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleBusinessException() {
        // 模拟抛出业务异常 (新人模式: 仅字符串)
        BusinessException ex = new BusinessException("余额不足");
        Result<Void> result = handler.handleBusinessException(ex);

        assertEquals(400, result.getCode());
        assertEquals("余额不足", result.getMessage());
    }

    @Test
    void testHandleException() {
        // 模拟不可预知的系统异常，如空指针
        NullPointerException ex = new NullPointerException("Something is null");
        Result<Void> result = handler.handleException(ex);

        // 验证兜底拦截器返回 500 和统一个提示
        assertEquals(500, result.getCode());
        assertEquals("系统内部繁忙，请稍后再试", result.getMessage());
    }

    @Test
    void testHandleValidationException() {
        // 模拟参数校验异常 (如 Hibernate Validator 的 @NotNull 触发)
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "参数不能为null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        Result<Void> result = handler.handleValidationException(ex);

        assertEquals(400, result.getCode());
        assertEquals("参数不能为null", result.getMessage());
    }
}
