package com.example.demo.task;

import com.example.demo.mapper.EbookMapper;
import com.example.demo.mapper.EbookSnapshotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统计定时任务：
 * 1. 电子书统计聚合（按文档汇总批量更新 ebook 表）
 * 2. 每日快照生成（对比昨日计算日增量）
 */
@Slf4j
@Component
public class StatsScheduledTask {

    private final EbookMapper ebookMapper;
    private final EbookSnapshotMapper ebookSnapshotMapper;

    public StatsScheduledTask(EbookMapper ebookMapper, EbookSnapshotMapper ebookSnapshotMapper) {
        this.ebookMapper = ebookMapper;
        this.ebookSnapshotMapper = ebookSnapshotMapper;
    }

    /**
     * 电子书统计聚合：按文档维度汇总阅读数/点赞数，批量更新 ebook 表的统计字段
     * cron 表达式从配置项 task.ebook-aggregate-cron 读取
     */
    @Scheduled(cron = "#{taskProperties.ebookAggregateCron}")
    public void aggregateEbookStats() {
        int updated = ebookMapper.aggregateStats();
        log.info("电子书统计聚合完成，更新 {} 本电子书", updated);
    }

    /**
     * 每日快照生成：将当日各电子书阅读/点赞数写入快照表，用于计算日增量和趋势图
     * cron 表达式从配置项 task.snapshot-cron 读取
     */
    @Scheduled(cron = "#{taskProperties.snapshotCron}")
    public void generateDailySnapshot() {
        int inserted = ebookSnapshotMapper.insertDailySnapshot();
        log.info("每日快照生成完成，新增 {} 条", inserted);
    }
}
