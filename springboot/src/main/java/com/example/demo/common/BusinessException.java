package com.example.demo.common;

import lombok.Getter;

/**
 * 业务异常：业务规则不满足时抛出，由全局异常处理器转为统一响应
 */
@Getter
public class BusinessException extends RuntimeException {

    // 业务错误码
    private final String code;

    /**
     * 仅传消息的构造：错误码默认为 SYSTEM_ERROR(500)
     */
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
    }

    /**
     * 指定错误码和消息的构造：用于明确业务错误类型
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
