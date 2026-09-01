package com.example.demo.service.impl;

import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.util.Md5Util;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User findByLoginName(String loginName) {
        return userMapper.selectByLoginName(loginName);
    }

    @Override
    public int createUser(User user) {
        return userMapper.insert(user);
    }

    @Override
    public User login(String loginName, String password) {
        User user = userMapper.selectByLoginName(loginName);
        if (user == null || !user.getPassword().equals(Md5Util.md5(password))) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST.getCode(), ErrorCode.USER_NOT_EXIST.getMessage());
        }
        return user;
    }
}
