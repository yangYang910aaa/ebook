package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {

    User findByLoginName(String loginName);

    int createUser(User user);

    User login(String loginName, String password);
}
