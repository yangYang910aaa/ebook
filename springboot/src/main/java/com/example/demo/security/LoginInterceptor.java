package com.example.demo.security;

import com.example.demo.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录拦截器：校验请求头 token，通过后把登录用户写入线程上下文
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public LoginInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 请求前置处理：校验请求头 token，从 Redis 取出对应用户并写入 ThreadLocal
     * - OPTIONS 预检请求直接放行（CORS 跨域需要）
     * - token 缺失或 Redis 中无对应会话时返回 403 风格的统一错误响应
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            writeForbidden(response);
            return false;
        }
        Object userObj = redisTemplate.opsForValue().get(token);
        if (userObj == null) {
            writeForbidden(response);
            return false;
        }
        User user = OBJECT_MAPPER.convertValue(userObj, User.class);
        // 将当前登录用户写入 ThreadLocal，供业务层通过 UserContext.get() 获取
        UserContext.set(user);
        return true;
    }

    /**
     * 请求完成后清理 ThreadLocal：必须在 afterCompletion 中移除，防止线程池复用时用户信息串用、内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 写入无权限响应：HTTP 状态 200 + 统一 JSON 体（前端按 success=false 处理）
     */
    private void writeForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "用户操作没有权限");
        body.put("content", null);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(body));
    }
}
