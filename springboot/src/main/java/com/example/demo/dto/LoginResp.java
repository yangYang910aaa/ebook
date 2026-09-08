package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应：登录成功后返回 token + 用户基本信息
 * token 作为 Redis 中用户会话的 key，后续请求放在请求头 token 字段
 */
@Data
@AllArgsConstructor
public class LoginResp {
    // 登录令牌（雪花ID，Redis中存储用户信息的key）
    private String token;
    // 用户ID
    private Long id;
    // 登录名
    private String loginName;
    // 姓名/昵称
    private String name;
}
