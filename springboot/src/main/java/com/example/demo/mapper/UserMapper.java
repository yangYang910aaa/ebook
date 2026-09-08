package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper：用户表的 CRUD、分页查询及登录名唯一性校验。
 * 密码存储为 MD5 加盐后的哈希值。SQL 见 resources/mapper/UserMapper.xml。
 */
@Mapper
public interface UserMapper {

    /** 按登录名查询用户（登录时使用） */
    User selectByLoginName(@Param("loginName") String loginName);

    /** 按 ID 查询用户 */
    User selectById(@Param("id") Long id);

    /** 按登录名统计用户数量（新增用户时校验唯一性） */
    long count(@Param("loginName") String loginName);

    /** 按登录名模糊分页查询用户 */
    List<User> selectPage(@Param("loginName") String loginName,
                          @Param("offset") int offset,
                          @Param("pageSize") int pageSize);

    /** 新增用户 */
    int insert(User user);

    /** 更新用户昵称（编辑用户时仅修改昵称） */
    int updateName(User user);

    /** 更新用户密码（重置密码时使用，密码已加盐 MD5） */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /** 按 ID 删除用户 */
    int deleteById(@Param("id") Long id);
}
