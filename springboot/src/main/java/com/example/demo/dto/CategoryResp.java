package com.example.demo.dto;

import lombok.Data;

/**
 * 分类响应（含父分类名称）：分类列表展示用
 */
@Data
public class CategoryResp {
    // 分类ID
    private Long id;
    // 父分类ID（0表示一级分类）
    private Long parent;
    // 分类名称
    private String name;
    // 同级排序号
    private Integer sort;
    // 父分类名称（关联查询填充，一级分类时为空）
    private String parentName;
}
