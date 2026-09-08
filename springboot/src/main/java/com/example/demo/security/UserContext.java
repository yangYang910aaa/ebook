package com.example.demo.security;

import com.example.demo.entity.User;

/**
 * 当前登录用户上下文（ThreadLocal），登录拦截器通过后写入，请求结束清理
 */
public class UserContext {

    // ThreadLocal 持有当前线程的登录用户，请求结束必须清理
    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    // 私有构造：工具类禁止实例化
    private UserContext() {
    }

    /**
     * 设置当前登录用户（登录拦截器调用）
     */
    public static void set(User user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前登录用户（业务层调用，未登录时返回 null）
     */
    public static User get() {
        return HOLDER.get();
    }

    /**
     * 清理当前线程的用户上下文（拦截器 afterCompletion 调用，防止内存泄漏）
     */
    public static void clear() {
        HOLDER.remove();
    }
}
