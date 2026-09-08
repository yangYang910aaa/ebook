package com.example.demo.service.impl;

import com.example.demo.dto.DailyStatResp;
import com.example.demo.dto.StatisticResp;
import com.example.demo.mapper.EbookSnapshotMapper;
import com.example.demo.service.EbookSnapshotService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 统计与快照服务实现：基于电子书每日快照计算运营统计数据。
 * 核心统计口径：
 * 1. 快照基线：每日 00:30 定时生成当天快照，记录该时刻各电子书的累计阅读/点赞；
 * 2. 今日增量 = 实时总量（ebook 表） - 最近一次快照基线；
 * 3. 昨日增量 = 最近两次快照的累计值之差；
 * 4. 预计今日阅读 = 今日增量 × (1440 / 已过分钟数)，按时间占比线性外推；
 * 5. 点赞率 = 总点赞 / 总阅读 × 100%。
 */
@Service
public class EbookSnapshotServiceImpl implements EbookSnapshotService {

    private final EbookSnapshotMapper ebookSnapshotMapper;

    public EbookSnapshotServiceImpl(EbookSnapshotMapper ebookSnapshotMapper) {
        this.ebookSnapshotMapper = ebookSnapshotMapper;
    }

    /**
     * 计算今日/昨日统计卡片：
     * - 总阅读/总点赞：ebook 表实时累计值；
     * - 今日增量：实时总量 - 最近一次快照基线（快照每日 00:30 生成）；
     * - 昨日增量：最近两次快照累计值之差；
     * - 预计今日阅读：按已过时间占全天比例线性外推；
     * - 点赞率：总点赞 / 总阅读；增长率：预计今日 vs 昨日增量。
     */
    @Override
    public StatisticResp getStatistic() {
        Map<String, Object> total = ebookSnapshotMapper.selectTotalStat();
        Map<String, Object> latest = ebookSnapshotMapper.selectLatestSnapshotStat();
        Map<String, Object> prev = ebookSnapshotMapper.selectPrevSnapshotStat();

        long totalView = toLong(total.get("view_count"));
        long totalVote = toLong(total.get("vote_count"));

        // 最近一次快照作为"今日"基线（快照每日 00:30 生成，值为该时刻累计量）
        // 今日阅读/点赞 = 实时总量 - 基线，即最近快照之后新增的量
        boolean hasSnapshot = latest.get("max_date") != null;
        long baselineView = hasSnapshot ? toLong(latest.get("view_count")) : 0;
        long baselineVote = hasSnapshot ? toLong(latest.get("vote_count")) : 0;
        long todayView = hasSnapshot ? Math.max(0, totalView - baselineView) : 0;
        long todayVote = hasSnapshot ? Math.max(0, totalVote - baselineVote) : 0;

        double voteRate = totalView == 0 ? 0.0 : round2(totalVote * 100.0 / totalView);

        // 预计今日阅读：按已过时间占全天比例线性外推（增量口径）
        int minutes = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        int elapsed = Math.max(minutes, 1);
        long estimatedToday = todayView * 1440L / elapsed;

        // 昨日阅读增量：最近两次快照累计之差（与快照日增量口径一致）
        long prevView = hasSnapshot ? toLong(prev.get("view_count")) : 0;
        long yesterdayView = hasSnapshot ? Math.max(0, baselineView - prevView) : 0;

        // 预计今日阅读增长率（对比昨日增量）
        double growth = yesterdayView > 0
                ? round2((estimatedToday - yesterdayView) * 100.0 / yesterdayView)
                : 0.0;

        StatisticResp resp = new StatisticResp();
        resp.setTotalViewCount(totalView);
        resp.setTotalVoteCount(totalVote);
        resp.setVoteRate(voteRate);
        resp.setTodayViewCount(todayView);
        resp.setTodayVoteCount(todayVote);
        resp.setYesterdayViewCount(yesterdayView);
        resp.setEstimatedTodayView(estimatedToday);
        resp.setEstimatedGrowth(growth);
        return resp;
    }

    /** 查询近 30 天每日阅读/点赞增量趋势（含今天，共 30 天） */
    @Override
    public List<DailyStatResp> get30Statistic() {
        String startDate = LocalDate.now().minusDays(29).toString();
        return ebookSnapshotMapper.selectLast30Days(startDate);
    }

    /** 将 MyBatis 返回的 Number（可能是 Long/BigDecimal）安全转为 long，null 视为 0 */
    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    /** 保留两位小数（四舍五入） */
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
