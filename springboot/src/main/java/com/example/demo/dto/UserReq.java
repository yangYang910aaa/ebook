package com.example.demo.dto;

import lombok.Data;

/**
 * 用户新增/编辑请求
 */
@Data
public class UserReq {
    private Long id;
    private String loginName;
    private String name;
    private String password;
}
