package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页结果：统一封装分页查询的返回数据，content 为 { total, list }
 *
 * @param <T> 列表元素类型
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    // 总记录数
    private Long total;
    // 当前页数据列表
    private List<T> list;
}
