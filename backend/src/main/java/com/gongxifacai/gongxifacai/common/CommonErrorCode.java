package com.gongxifacai.gongxifacai.common;

/**
 * 通用错误码枚举
 * 如果不会就忽略它
 */
public enum CommonErrorCode implements ErrorCode {

    SUCCESS(200, "Success"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource not found"),
    SYSTEM_ERROR(500, "Internal server error");

    private final Integer code;
    private final String message;

    CommonErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
