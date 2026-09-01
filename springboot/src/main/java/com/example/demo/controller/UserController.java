package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.LoginReq;
import com.example.demo.dto.LoginResp;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.SnowflakeIdWorker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 用户认证：登录 / 退出
 */
@Tag(name = "用户认证")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserController(UserService userService, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "登录")
    @PostMapping("/userLogin")
    public Result<LoginResp> login(@RequestBody LoginReq req) {
        User user = userService.login(req.getLoginName(), req.getPassword());
        String token = String.valueOf(SnowflakeIdWorker.getInstance().nextId());
        redisTemplate.opsForValue().set(token, user, 24, TimeUnit.HOURS);
        LoginResp resp = new LoginResp(token, user.getId(), user.getLoginName(), user.getName());
        return Result.success(resp);
    }

    @Operation(summary = "退出登录")
    @GetMapping("/logout/{token}")
    public Result<Void> logout(@PathVariable String token) {
        redisTemplate.delete(token);
        return Result.success();
    }
}
