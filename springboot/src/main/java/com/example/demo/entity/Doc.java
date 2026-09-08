package com.example.demo.entity;

import lombok.Data;

/**
 * 文档（章节）实体：电子书下的章节内容，自身支持树形结构（parent=0 为顶级章节）。
 * 富文本内容存储在 Content 表中，与 Doc 1:1 且主键相同。
 */
@Data
public class Doc {
    private Long id;
    private Long ebookId;    // 所属电子书 ID
    private Long parent;     // 父文档 ID，0 表示顶级章节
    private String name;     // 文档（章节）名称
    private Integer sort;    // 同级排序号
    private Integer viewCount; // 阅读数
    private Integer voteCount; // 点赞数
}
