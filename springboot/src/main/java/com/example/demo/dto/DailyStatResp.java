package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单日统计（30 天趋势用）：每日阅读/点赞增量数据点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatResp {
    // 统计日期（yyyy-MM-dd）
    private String date;
    // 当日阅读增量（今日快照 - 昨日快照）
    private Long viewIncrease;
    // 当日点赞增量
    private Long voteIncrease;
}
