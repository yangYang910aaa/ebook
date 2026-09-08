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

    // 是否成功
    private Boolean success;
    // 提示消息
    private String message;
    // 响应数据（分页时为 PageResult { total, list }）
    private T content;

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, "success", data);
    }

    /**
     * 成功响应（无数据，如删除/更新操作）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应（带错误消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(false, message, null);
    }
}
