package com.example.demo.dto;

import lombok.Data;

/**
 * 首页统计卡片数据
 */
@Data
public class StatisticResp {
    private Long totalViewCount;
    private Long totalVoteCount;
    private Double voteRate;
    private Long todayViewCount;
    private Long todayVoteCount;
    private Long yesterdayViewCount;
    private Long estimatedTodayView;
    private Double estimatedGrowth;
}
