package com.example.demo.entity;

import lombok.Data;

/**
 * 文档富文本内容（与 doc 1:1，主键相同）
 */
@Data
public class Content {
    private Long id;
    private String content;
}
