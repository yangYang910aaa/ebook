package com.example.demo.dto;

import lombok.Data;

/**
 * 电子书响应（含分类名称）
 */
@Data
public class EbookResp {
    private Long id;
    private String name;
    private Long category1Id;
    private Long category2Id;
    private String category1Name;
    private String category2Name;
    private String description;
    private String cover;
    private Integer docCount;
    private Integer viewCount;
    private Integer voteCount;
}
