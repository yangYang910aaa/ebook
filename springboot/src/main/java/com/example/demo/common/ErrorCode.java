package com.example.demo.common;

import lombok.Getter;

/**
 * 错误码枚举（随业务按需扩充）
 */
@Getter
public enum ErrorCode {

    SYSTEM_ERROR("500", "系统异常"),
    PARAM_ERROR("400", "参数错误"),
    NOT_FOUND("404", "资源不存在"),
    FORBIDDEN("403", "用户操作没有权限"),
    USER_EXIST("1001", "登录名已存在"),
    USER_NOT_EXIST("1002", "用户名不存在或密码错误");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
