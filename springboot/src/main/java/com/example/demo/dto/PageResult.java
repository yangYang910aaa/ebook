package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页结果：content 为 { total, list }
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> list;
}
