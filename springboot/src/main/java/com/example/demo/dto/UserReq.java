package com.example.demo.dto;

import lombok.Data;

/**
 * 用户新增/编辑请求
 */
@Data
public class UserReq {
    // 用户ID（新增时为空，编辑时必填）
    private Long id;
    // 登录名（唯一）
    private String loginName;
    // 姓名/昵称
    private String name;
    // 密码（前端已MD5加密，后端再MD5一次存储）
    private String password;
}
