package com.example.demo.dto;

import lombok.Data;

/**
 * 重置密码请求：管理员重置指定用户密码
 */
@Data
public class ResetPwdReq {
    // 目标用户ID
    private Long id;
    // 新密码（前端已MD5加密，后端再MD5一次存储）
    private String password;
}
