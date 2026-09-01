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
        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

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
