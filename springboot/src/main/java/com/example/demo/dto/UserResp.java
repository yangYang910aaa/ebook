package com.example.demo.dto;

import lombok.Data;

/**
 * 用户响应（含密码 MD5 密文：需求 FR-12 要求后台表格展示"密码（密文）"列）
 */
@Data
public class UserResp {
    private Long id;
    private String loginName;
    private String name;
    private String password;
}
