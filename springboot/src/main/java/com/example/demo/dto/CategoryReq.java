package com.example.demo.dto;

import lombok.Data;

/**
 * 分类新增/编辑请求
 */
@Data
public class CategoryReq {
    private Long id;
    private Long parent;
    private String name;
    private Integer sort;
}
