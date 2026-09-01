package com.example.demo.entity;

import lombok.Data;

/**
 * 文档（章节），自身树形
 */
@Data
public class Doc {
    private Long id;
    private Long ebookId;
    private Long parent;
    private String name;
    private Integer sort;
    private Integer viewCount;
    private Integer voteCount;
}
