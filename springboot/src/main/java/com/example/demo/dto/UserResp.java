package com.example.demo.dto;

import lombok.Data;

/**
 * 用户响应（不含密码）
 */
@Data
public class UserResp {
    private Long id;
    private String loginName;
    private String name;
}
