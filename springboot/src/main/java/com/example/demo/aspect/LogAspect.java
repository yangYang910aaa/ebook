package com.example.demo.aspect;

import com.example.demo.util.SnowflakeIdWorker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller 请求日志切面：雪花流水号（MDC LOG_ID）+ 参数脱敏 + 耗时
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("execution(* com.example.demo.controller..*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String logId = String.valueOf(SnowflakeIdWorker.getInstance().nextId());
        org.slf4j.MDC.put("LOG_ID", logId);

        String uri = "";
        String method = "";
        String ip = "";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            uri = request.getRequestURI();
            method = request.getMethod();
            ip = request.getRemoteAddr();
        }

        String params = maskArgs(joinPoint.getArgs());
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("[{}] {} {} ip={} params={} 耗时={}ms 返回={}",
                    logId, method, uri, ip, params, cost, result == null ? "null" : result);
            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[{}] {} {} ip={} params={} 耗时={}ms 异常={}",
                    logId, method, uri, ip, params, cost, e.getMessage());
            throw e;
        } finally {
            org.slf4j.MDC.remove("LOG_ID");
        }
    }

    private String maskArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                parts.add("null");
            } else if (arg instanceof MultipartFile) {
                parts.add("file=***");
            } else if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            } else if (arg instanceof Map) {
                parts.add(maskMap((Map<?, ?>) arg));
            } else if (isSimpleType(arg)) {
                parts.add(String.valueOf(arg));
            } else {
                parts.add(maskBean(arg));
            }
        }
        return String.join(", ", parts);
    }

    private String maskMap(Map<?, ?> map) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            if (key.toLowerCase().contains("password") || key.toLowerCase().contains("file")) {
                parts.add(key + "=***");
            } else {
                parts.add(key + "=" + entry.getValue());
            }
        }
        return "{" + String.join(", ", parts) + "}";
    }

    private String maskBean(Object obj) {
        try {
            List<String> parts = new ArrayList<>();
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(obj);
                if (name.toLowerCase().contains("password") || name.toLowerCase().contains("file")) {
                    parts.add(name + "=***");
                } else {
                    parts.add(name + "=" + value);
                }
            }
            return "{" + String.join(", ", parts) + "}";
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private boolean isSimpleType(Object arg) {
        return arg instanceof String
                || arg instanceof Number
                || arg instanceof Boolean
                || arg instanceof Character
                || arg instanceof Enum<?>;
    }
}
