package com.example.demo.service;

import com.example.demo.dto.DailyStatResp;
import com.example.demo.dto.StatisticResp;

import java.util.List;

/**
 * 统计与快照服务
 */
public interface EbookSnapshotService {

    StatisticResp getStatistic();

    List<DailyStatResp> get30Statistic();
}
