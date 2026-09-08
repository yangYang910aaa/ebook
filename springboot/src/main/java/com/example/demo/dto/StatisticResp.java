package com.example.demo.dto;

import lombok.Data;

/**
 * 首页统计卡片数据：累计、今日、昨日及预估指标
 */
@Data
public class StatisticResp {
    // 累计阅读数（全量电子书汇总）
    private Long totalViewCount;
    // 累计点赞数
    private Long totalVoteCount;
    // 点赞率 = 累计点赞数 / 累计阅读数
    private Double voteRate;
    // 今日阅读数（当日快照值）
    private Long todayViewCount;
    // 今日点赞数
    private Long todayVoteCount;
    // 昨日阅读数（用于环比对比）
    private Long yesterdayViewCount;
    // 今日预估阅读数（按当前时段进度线性推算）
    private Long estimatedTodayView;
    // 预估增长率 = (今日预估 - 昨日) / 昨日
    private Double estimatedGrowth;
}
