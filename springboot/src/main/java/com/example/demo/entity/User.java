package com.example.demo.entity;

import lombok.Data;

/**
 * 用户实体：系统登录用户，密码存储为 MD5 加盐后的哈希值（前端 MD5 + 后端加盐）。
 * 登录态通过 Redis Token 管理，不依赖 Session。
 */
@Data
public class User {
    private Long id;
    private String loginName; // 登录名（唯一）
    private String name;      // 昵称/显示名
    private String password;  // 密码（MD5 加盐后的哈希值）
}
