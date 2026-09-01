package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper（SQL 见 resources/mapper/UserMapper.xml）
 */
@Mapper
public interface UserMapper {

    User selectByUsername(@Param("username") String username);

    int insert(User user);
}
