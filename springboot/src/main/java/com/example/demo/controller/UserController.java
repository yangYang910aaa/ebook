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
 * 用户认证与管理控制器：提供登录/退出（基于 Redis Token 的会话管理）、用户分页查询、
 * 新增/编辑、重置密码、删除等接口。路径前缀 /user。
 * 登录采用 MD5 加盐校验，Token 存入 Redis 并设置 24 小时过期。
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

    /**
     * POST /user/userLogin — 用户登录：校验账号密码后生成雪花 ID 作为 Token，
     * 将用户信息存入 Redis（24 小时过期），返回 Token 及用户基本信息。
     */
    @Operation(summary = "登录")
    @PostMapping("/userLogin")
    public Result<LoginResp> login(@RequestBody LoginReq req) {
        User user = userService.login(req.getLoginName(), req.getPassword());
        String token = String.valueOf(SnowflakeIdWorker.getInstance().nextId());
        redisTemplate.opsForValue().set(token, user, Duration.ofHours(24));
        LoginResp resp = new LoginResp(token, user.getId(), user.getLoginName(), user.getName());
        return Result.success(resp);
    }

    /** GET /user/logout/{token} — 退出登录：从 Redis 删除当前 Token，使会话失效 */
    @Operation(summary = "退出登录")
    @GetMapping("/logout/{token}")
    public Result<Void> logout(@PathVariable String token) {
        redisTemplate.delete(token);
        return Result.success();
    }

    /** GET /user/getUserListByPage — 用户分页查询，支持按登录名模糊筛选 */
    @Operation(summary = "用户分页查询")
    @GetMapping("/getUserListByPage")
    public Result<PageResult<UserResp>> list(@RequestParam(required = false) String loginName,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.list(loginName, new PageReq(pageNum, pageSize)));
    }

    /** POST /user/save — 新增或编辑用户；新增时校验登录名唯一性并加密密码，编辑仅修改昵称 */
    @Operation(summary = "新增/编辑用户")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody UserReq req) {
        userService.save(req);
        return Result.success();
    }

    /** POST /user/resetPassword — 重置指定用户密码（新密码经前端 MD5 后，后端再次加盐存储） */
    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestBody ResetPwdReq req) {
        userService.resetPassword(req);
        return Result.success();
    }

    /** GET /user/remove?id= — 删除指定用户 */
    @Operation(summary = "删除用户")
    @GetMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        userService.remove(id);
        return Result.success();
    }
}
