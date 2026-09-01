package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper（SQL 见 resources/mapper/UserMapper.xml）
 */
@Mapper
public interface UserMapper {

    User selectByLoginName(@Param("loginName") String loginName);

    User selectById(@Param("id") Long id);

    long count(@Param("loginName") String loginName);

    List<User> selectPage(@Param("loginName") String loginName,
                          @Param("offset") int offset,
                          @Param("pageSize") int pageSize);

    int insert(User user);

    int updateName(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int deleteById(@Param("id") Long id);
}
