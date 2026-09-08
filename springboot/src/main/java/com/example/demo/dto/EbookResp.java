package com.example.demo.dto;

import lombok.Data;

/**
 * 电子书响应（含分类名称）：电子书列表/详情展示用
 */
@Data
public class EbookResp {
    // 电子书ID
    private Long id;
    // 电子书名称
    private String name;
    // 一级分类ID
    private Long category1Id;
    // 二级分类ID
    private Long category2Id;
    // 一级分类名称（关联查询填充）
    private String category1Name;
    // 二级分类名称（关联查询填充）
    private String category2Name;
    // 电子书描述
    private String description;
    // 封面图片URL
    private String cover;
    // 文档数量（该电子书下的文档总数）
    private Integer docCount;
    // 阅读数
    private Integer viewCount;
    // 点赞数
    private Integer voteCount;
}
