package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 定时任务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "task")
public class TaskProperties {

    /** 电子书统计聚合 cron */
    private String ebookAggregateCron;

    /** 每日快照 cron */
    private String snapshotCron;
}
