package com.example.demo.dto;

import lombok.Data;

/**
 * 用户响应（含密码 MD5 密文：需求 FR-12 要求后台表格展示"密码（密文）"列）
 */
@Data
public class UserResp {
    // 用户ID
    private Long id;
    // 登录名
    private String loginName;
    // 姓名/昵称
    private String name;
    // 密码MD5密文（仅后台管理展示用）
    private String password;
}
