package com.example.demo.service.impl;

import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.ResetPwdReq;
import com.example.demo.dto.UserReq;
import com.example.demo.dto.UserResp;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.util.Md5Util;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务实现：负责用户登录认证（MD5 加盐校验）、分页查询、新增/编辑、重置密码及删除。
 * 密码策略：前端已对明文做一次 MD5 再提交，后端收到 32 位 MD5 十六进制后再次加盐 MD5 存储，
 * 实现"前端+后端双重加密"。登录校验时对前端传入的 MD5 值再次加盐后与库中存储比对。
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * 前端已对明文做一次 MD5 再提交（需求"前端+后端双重加密"），
     * 后端校验收到的是 32 位 MD5 十六进制；明文规则（6~32 位含数字英文）由前端加密前校验。
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户登录：按登录名查询用户，将前端传入的 MD5 密码再次加盐后与库中存储比对，
     * 用户不存在或密码不匹配均抛出"用户不存在"异常（不区分原因，避免账号枚举攻击）。
     */
    @Override
    public User login(String loginName, String password) {
        User user = userMapper.selectByLoginName(loginName);
        if (user == null || !user.getPassword().equals(Md5Util.md5(password))) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST.getCode(), ErrorCode.USER_NOT_EXIST.getMessage());
        }
        return user;
    }

    /** 用户分页查询：修正非法分页参数，pageSize 上限 1000，按登录名模糊筛选 */
    @Override
    public PageResult<UserResp> list(String loginName, PageReq pageReq) {
        int pageNum = pageReq.getPageNum() == null || pageReq.getPageNum() < 1 ? 1 : pageReq.getPageNum();
        int pageSize = pageReq.getPageSize() == null || pageReq.getPageSize() < 1 ? 10 : pageReq.getPageSize();
        pageSize = Math.min(pageSize, 1000);
        long total = userMapper.count(loginName);
        List<User> users = userMapper.selectPage(loginName, (pageNum - 1) * pageSize, pageSize);
        List<UserResp> list = new ArrayList<>();
        for (User user : users) {
            UserResp resp = new UserResp();
            resp.setId(user.getId());
            resp.setLoginName(user.getLoginName());
            resp.setName(user.getName());
            resp.setPassword(user.getPassword());
            list.add(resp);
        }
        return new PageResult<>(total, list);
    }

    /**
     * 新增或编辑用户：
     * - 新增：校验登录名非空且唯一，校验密码格式（32位MD5十六进制），密码加盐后存储；
     * - 编辑：仅修改昵称，登录名与密码不在此接口修改（密码走重置密码接口）。
     */
    @Override
    public void save(UserReq req) {
        if (req.getId() == null) {
            if (req.getLoginName() == null || req.getLoginName().isBlank()) {
                throw new BusinessException("登录名不能为空");
            }
            if (userMapper.count(req.getLoginName()) > 0) {
                throw new BusinessException(ErrorCode.USER_EXIST.getCode(), ErrorCode.USER_EXIST.getMessage());
            }
            validatePassword(req.getPassword());
            User user = new User();
            user.setLoginName(req.getLoginName());
            user.setName(req.getName());
            user.setPassword(Md5Util.md5(req.getPassword()));
            userMapper.insert(user);
        } else {
            // 编辑仅修改昵称；登录名与密码在此不修改
            User user = new User();
            user.setId(req.getId());
            user.setName(req.getName());
            userMapper.updateName(user);
        }
    }

    /** 重置密码：校验新密码格式后，加盐 MD5 存储 */
    @Override
    public void resetPassword(ResetPwdReq req) {
        validatePassword(req.getPassword());
        userMapper.updatePassword(req.getId(), Md5Util.md5(req.getPassword()));
    }

    /** 删除指定用户 */
    @Override
    public void remove(Long id) {
        userMapper.deleteById(id);
    }

    /** 校验密码格式：必须为 32 位 MD5 十六进制字符串（前端已加密一次后的格式） */
    private void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException("密码格式不正确");
        }
    }
}
