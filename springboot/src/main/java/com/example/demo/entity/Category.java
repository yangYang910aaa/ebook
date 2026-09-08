package com.example.demo.entity;

import lombok.Data;

/**
 * 分类实体：电子书分类，两级树形结构（parent=0 为一级分类，一级分类下挂载二级分类）。
 * 电子书通过 category1Id/category2Id 关联分类。
 */
@Data
public class Category {
    private Long id;
    private Long parent;   // 父分类 ID，0 表示一级分类
    private String name;   // 分类名称
    private Integer sort;  // 同级排序号
}
