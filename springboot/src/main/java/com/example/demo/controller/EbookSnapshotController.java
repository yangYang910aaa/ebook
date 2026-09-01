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
 * 数据统计与报表
 */
@Tag(name = "数据统计")
@RestController
@RequestMapping("/ebook-snapshot")
public class EbookSnapshotController {

    private final EbookSnapshotService ebookSnapshotService;

    public EbookSnapshotController(EbookSnapshotService ebookSnapshotService) {
        this.ebookSnapshotService = ebookSnapshotService;
    }

    @Operation(summary = "昨日/今日统计卡片")
    @GetMapping("/getStatistic")
    public Result<StatisticResp> getStatistic() {
        return Result.success(ebookSnapshotService.getStatistic());
    }

    @Operation(summary = "近 30 天趋势")
    @GetMapping("/get30Statistic")
    public Result<List<DailyStatResp>> get30Statistic() {
        return Result.success(ebookSnapshotService.get30Statistic());
    }
}
