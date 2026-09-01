package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper（SQL 见 resources/mapper/UserMapper.xml）
 */
@Mapper
public interface UserMapper {

    User selectByLoginName(@Param("loginName") String loginName);

    int insert(User user);
}
