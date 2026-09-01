package com.example.demo.controller;

import com.example.demo.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "系统接口")
@RestController
@RequestMapping("/api")
public class HelloController {

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
