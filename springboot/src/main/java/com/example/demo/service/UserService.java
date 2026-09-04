package com.example.demo.service;

import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.ResetPwdReq;
import com.example.demo.dto.UserReq;
import com.example.demo.dto.UserResp;
import com.example.demo.entity.User;

public interface UserService {

    User login(String loginName, String password);

    PageResult<UserResp> list(String loginName, PageReq pageReq);

    void save(UserReq req);

    void resetPassword(ResetPwdReq req);

    void remove(Long id);
}
