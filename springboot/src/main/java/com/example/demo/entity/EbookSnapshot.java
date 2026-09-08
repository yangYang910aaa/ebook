package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDate;

/**
 * 电子书每日统计快照：定时任务每日生成，记录各电子书当天的累计阅读/点赞及日增量。
 * 用于统计卡片和近 30 天趋势计算。
 */
@Data
public class EbookSnapshot {
    private Long id;
    private Long ebookId;       // 所属电子书 ID
    private LocalDate date;     // 快照日期
    private Integer viewCount;  // 截至该日的累计阅读数
    private Integer voteCount;  // 截至该日的累计点赞数
    private Integer viewIncrease; // 当日阅读增量（= 今日累计 - 昨日累计）
    private Integer voteIncrease; // 当日点赞增量
}
