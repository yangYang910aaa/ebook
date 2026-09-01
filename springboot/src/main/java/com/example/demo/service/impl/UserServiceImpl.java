package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;

/**
 * 用户服务实现。
 * 注意：当前未加 @Service 注解，因为还没有 MySQL 数据源（MyBatis 的 Mapper Bean 未创建）。
 * 接入数据库后：1) 启用 application.yml 里的数据源配置  2) 给本类加上 @Service 注解。
 */
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public int createUser(User user) {
        return userMapper.insert(user);
    }
}
