package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 演示登录：直接签发 JWT，不校验密码。
     * 接入 MySQL 后，替换为 UserService 按用户名查询并比对密码。
     */
    @Operation(summary = "登录（演示）")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Result.error(400, "用户名不能为空");
        }
        String token = jwtUtil.generateToken(request.getUsername());
        return Result.success(Map.of("token", token));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
