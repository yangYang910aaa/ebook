package com.example.demo.controller;

import com.example.demo.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统接口控制器：提供健康检查接口，用于服务存活探测与运行环境信息查看。
 * 路径前缀 /api。
 */
@Tag(name = "系统接口")
@RestController
@RequestMapping("/api")
public class HelloController {

    /** GET /api/hello — 健康检查，返回服务问候语、Java 版本及当前时间戳 */
    @Operation(summary = "健康检查")
    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Hello, E-Book Platform!");
        data.put("java", System.getProperty("java.version"));
        data.put("time", System.currentTimeMillis());
        return Result.success(data);
    }
}
