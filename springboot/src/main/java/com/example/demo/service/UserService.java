package com.example.demo.service;

import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.ResetPwdReq;
import com.example.demo.dto.UserReq;
import com.example.demo.dto.UserResp;
import com.example.demo.entity.User;

/**
 * 用户服务接口：定义用户登录认证、分页查询、新增/编辑、重置密码及删除等业务操作。
 * 登录采用 MD5 加盐校验，密码存储为二次加密后的哈希值。
 */
public interface UserService {

    /**
     * 用户登录：根据登录名查询用户，校验密码（MD5 加盐），成功返回用户实体。
     * @param loginName 登录名
     * @param password 前端已 MD5 后的密码字符串
     * @return 登录成功的用户实体
     */
    User login(String loginName, String password);

    /**
     * 用户分页查询，支持按登录名模糊筛选。
     * @param loginName 登录名筛选条件（可空）
     * @param pageReq 分页参数
     * @return 分页结果
     */
    PageResult<UserResp> list(String loginName, PageReq pageReq);

    /**
     * 新增或编辑用户：新增时校验登录名唯一性并加密密码；编辑仅修改昵称。
     * @param req 用户请求参数
     */
    void save(UserReq req);

    /**
     * 重置指定用户密码，新密码经 MD5 加盐后存储。
     * @param req 重置密码请求（含用户 ID 和新密码）
     */
    void resetPassword(ResetPwdReq req);

    /**
     * 删除指定用户。
     * @param id 用户 ID
     */
    void remove(Long id);
}
