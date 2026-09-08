package com.example.demo.service;

import com.example.demo.dto.DailyStatResp;
import com.example.demo.dto.StatisticResp;

import java.util.List;

/**
 * 统计与快照服务接口：基于电子书每日快照提供运营统计数据。
 * 快照由定时任务每日增量生成，统计口径包含实时总量与快照基线的差值计算。
 */
public interface EbookSnapshotService {

    /**
     * 获取今日/昨日统计卡片：总阅读、总点赞、点赞率、今日增量、昨日增量、预计今日阅读及增长率。
     * @return 统计响应
     */
    StatisticResp getStatistic();

    /**
     * 获取近 30 天每日阅读/点赞增量趋势。
     * @return 近 30 天每日统计列表
     */
    List<DailyStatResp> get30Statistic();
}
