package com.gongxifacai.gongxifacai.common;

/**
 * 通用错误码枚举
 * 如果不会就忽略它
 */
public enum CommonErrorCode implements ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数或业务逻辑错误"),
    UNAUTHORIZED(401, "未授权或认证失败"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    SYSTEM_ERROR(500, "系统内部繁忙，请稍后再试"),
    USER_NOT_FOUND(404, "用户不存在");

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
