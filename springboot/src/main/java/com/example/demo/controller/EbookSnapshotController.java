package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.DailyStatResp;
import com.example.demo.dto.StatisticResp;
import com.example.demo.service.EbookSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据统计控制器：基于电子书每日快照提供运营统计接口，包括今日/昨日概览卡片和近 30 天趋势。
 * 路径前缀 /ebook-snapshot，快照由定时任务每日增量生成。
 */
@Tag(name = "数据统计")
@RestController
@RequestMapping("/ebook-snapshot")
public class EbookSnapshotController {

    private final EbookSnapshotService ebookSnapshotService;

    public EbookSnapshotController(EbookSnapshotService ebookSnapshotService) {
        this.ebookSnapshotService = ebookSnapshotService;
    }

    /** GET /ebook-snapshot/getStatistic — 今日/昨日统计卡片：总阅读、总点赞、点赞率、今日增量、预计今日及增长率 */
    @Operation(summary = "昨日/今日统计卡片")
    @GetMapping("/getStatistic")
    public Result<StatisticResp> getStatistic() {
        return Result.success(ebookSnapshotService.getStatistic());
    }

    /** GET /ebook-snapshot/get30Statistic — 近 30 天每日阅读/点赞增量趋势 */
    @Operation(summary = "近 30 天趋势")
    @GetMapping("/get30Statistic")
    public Result<List<DailyStatResp>> get30Statistic() {
        return Result.success(ebookSnapshotService.get30Statistic());
    }
}
