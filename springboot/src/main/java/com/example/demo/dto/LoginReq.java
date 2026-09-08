package com.example.demo.dto;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginReq {
    // 登录名
    private String loginName;
    // 密码（前端已MD5加密一次，后端再MD5一次后比对）
    private String password;
}
