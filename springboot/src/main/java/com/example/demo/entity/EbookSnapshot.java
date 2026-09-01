package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDate;

/**
 * 电子书每日统计快照
 */
@Data
public class EbookSnapshot {
    private Long id;
    private Long ebookId;
    private LocalDate date;
    private Integer viewCount;
    private Integer voteCount;
    private Integer viewIncrease;
    private Integer voteIncrease;
}
