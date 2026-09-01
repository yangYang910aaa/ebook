package com.example.demo.config;

import com.example.demo.security.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：登录拦截器注册 + CORS 跨域
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] EXCLUDE_PATHS = {
            "/user/userLogin",
            "/ebook/query",
            "/category/getCategoryList",
            "/doc/all",
            "/doc/find-content/**",
            "/doc/vote/**",
            "/ebook-snapshot/**",
            "/ws/**",
            "/api/hello",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:8080", "http://localhost:8081")
                .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
