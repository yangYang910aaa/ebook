package com.example.demo.dto;

import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class ResetPwdReq {
    private Long id;
    private String password;
}
