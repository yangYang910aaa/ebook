package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单日统计（30 天趋势用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatResp {
    private String date;
    private Long viewIncrease;
    private Long voteIncrease;
}
