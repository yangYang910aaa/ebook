package com.example.demo.entity;

import lombok.Data;

/**
 * 电子书实体：系统核心业务对象，归属两级分类，包含多个文档（章节）。
 * docCount/viewCount/voteCount 由定时任务从文档表聚合回写。
 */
@Data
public class Ebook {
    private Long id;
    private String name;          // 电子书名称
    private Long category1Id;     // 一级分类 ID
    private Long category2Id;     // 二级分类 ID
    private String description;   // 电子书简介
    private String cover;         // 封面图片 URL
    private Integer docCount;     // 文档（章节）总数，定时聚合
    private Integer viewCount;    // 累计阅读数，定时聚合
    private Integer voteCount;    // 累计点赞数，定时聚合
}
