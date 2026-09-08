package com.example.demo.dto;

import lombok.Data;

/**
 * 分类新增/编辑请求
 */
@Data
public class CategoryReq {
    // 分类ID（新增时为空，编辑时必填）
    private Long id;
    // 父分类ID（0表示一级分类）
    private Long parent;
    // 分类名称
    private String name;
    // 同级排序号
    private Integer sort;
}
