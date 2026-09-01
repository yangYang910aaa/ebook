package com.example.demo.dto;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginReq {
    private String loginName;
    private String password;
}
