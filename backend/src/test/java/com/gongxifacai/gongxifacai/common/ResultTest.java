package com.gongxifacai.gongxifacai.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void testSuccessWithData() {
        String testData = "test-portfolio";
        Result<String> result = Result.success(testData);

        assertEquals(200, result.getCode());
        assertEquals("Success", result.getMessage());
        assertEquals(testData, result.getData());
    }

    @Test
    void testSuccessWithoutData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("Success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testErrorWithCodeAndMessage() {
        Result<Void> result = Result.error(4001, "Custom Error");

        assertEquals(4001, result.getCode());
        assertEquals("Custom Error", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testErrorWithErrorCodeEnum() {
        Result<Void> result = Result.error(CommonErrorCode.FORBIDDEN);

        assertEquals(403, result.getCode());
        assertEquals("没有权限访问", result.getMessage());
        assertNull(result.getData());
    }
}
