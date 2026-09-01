package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / springdoc 文档信息
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI ebookOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("数字电子书平台 API")
                .description("Spring Boot + MyBatis 后端接口文档")
                .version("v1.0.0"));
    }
}
