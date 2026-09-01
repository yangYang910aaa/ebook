package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.LoginReq;
import com.example.demo.dto.LoginResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.ResetPwdReq;
import com.example.demo.dto.UserReq;
import com.example.demo.dto.UserResp;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

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
        redisTemplate.opsForValue().set(token, user, Duration.ofHours(24));
        LoginResp resp = new LoginResp(token, user.getId(), user.getLoginName(), user.getName());
        return Result.success(resp);
    }

    @Operation(summary = "退出登录")
    @GetMapping("/logout/{token}")
    public Result<Void> logout(@PathVariable String token) {
        redisTemplate.delete(token);
        return Result.success();
    }

    @Operation(summary = "用户分页查询")
    @GetMapping("/getUserListByPage")
    public Result<PageResult<UserResp>> list(@RequestParam(required = false) String loginName,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.list(loginName, new PageReq(pageNum, pageSize)));
    }

    @Operation(summary = "新增/编辑用户")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody UserReq req) {
        userService.save(req);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestBody ResetPwdReq req) {
        userService.resetPassword(req);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @GetMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        userService.remove(id);
        return Result.success();
    }
}
