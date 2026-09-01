package com.example.demo.entity;

import lombok.Data;

/**
 * 分类（两级树形，parent=0 为一级）
 */
@Data
public class Category {
    private Long id;
    private Long parent;
    private String name;
    private Integer sort;
}
