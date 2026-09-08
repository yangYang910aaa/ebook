package com.example.demo.common;

import lombok.Getter;

/**
 * 错误码枚举（随业务按需扩充）
 */
@Getter
public enum ErrorCode {

    // 系统级错误
    SYSTEM_ERROR("500", "系统异常"),
    // 请求参数校验失败
    PARAM_ERROR("400", "参数错误"),
    // 请求资源不存在
    NOT_FOUND("404", "资源不存在"),
    // 未登录或 token 无效
    FORBIDDEN("403", "用户操作没有权限"),
    // 业务：登录名已被注册
    USER_EXIST("1001", "登录名已存在"),
    // 业务：登录名不存在或密码错误（统一提示，不区分是哪种，防止账号枚举）
    USER_NOT_EXIST("1002", "用户名不存在或密码错误");

    // 错误码
    private final String code;
    // 错误描述
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
