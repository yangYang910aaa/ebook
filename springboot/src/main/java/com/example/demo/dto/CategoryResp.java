package com.example.demo.dto;

import lombok.Data;

/**
 * 分类响应（含父分类名称）
 */
@Data
public class CategoryResp {
    private Long id;
    private Long parent;
    private String name;
    private Integer sort;
    private String parentName;
}
