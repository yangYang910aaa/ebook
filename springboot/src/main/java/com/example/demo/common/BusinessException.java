package com.example.demo.common;

import lombok.Getter;

/**
 * 业务异常：业务规则不满足时抛出，由全局异常处理器转为统一响应
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
