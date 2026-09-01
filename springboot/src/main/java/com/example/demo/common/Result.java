package com.example.demo.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体：{ success, message, content }
 * 分页数据 content 为 { total, list }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Boolean success;
    private String message;
    private T content;

    public static <T> Result<T> success(T data) {
        return new Result<>(true, "success", data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(false, message, null);
    }
}
