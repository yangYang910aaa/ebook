package com.example.demo.security;

import com.example.demo.entity.User;

/**
 * 当前登录用户上下文（ThreadLocal），登录拦截器通过后写入，请求结束清理
 */
public class UserContext {

    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(User user) {
        HOLDER.set(user);
    }

    public static User get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
