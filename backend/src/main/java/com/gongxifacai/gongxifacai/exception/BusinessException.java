package com.gongxifacai.gongxifacai.exception;

import com.gongxifacai.gongxifacai.common.CommonErrorCode;
import com.gongxifacai.gongxifacai.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码
     */
    private final Integer code;

    /**
     * 使用Enum，推荐
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 覆盖默认的Message，适用于需要动态拼接信息的场景，比如你想带上用户名之类的
     * 别传敏感信息
     */
    public BusinessException(ErrorCode errorCode, String dynamicMessage) {
        super(dynamicMessage);
        this.code = errorCode.getCode();
    }

    /**
     * 只传字符串，自动绑定错误码400，如果不明白就用这个
     * 别传敏感信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = CommonErrorCode.BAD_REQUEST.getCode();
    }
}
