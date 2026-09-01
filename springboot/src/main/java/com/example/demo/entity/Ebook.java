package com.example.demo.entity;

import lombok.Data;

/**
 * 电子书
 */
@Data
public class Ebook {
    private Long id;
    private String name;
    private Long category1Id;
    private Long category2Id;
    private String description;
    private String cover;
    private Integer docCount;
    private Integer viewCount;
    private Integer voteCount;
}
