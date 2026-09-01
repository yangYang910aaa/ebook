package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应：token + 用户基本信息
 */
@Data
@AllArgsConstructor
public class LoginResp {
    private String token;
    private Long id;
    private String loginName;
    private String name;
}
