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
 * 统计与快照服务实现
 */
@Service
public class EbookSnapshotServiceImpl implements EbookSnapshotService {

    private final EbookSnapshotMapper ebookSnapshotMapper;

    public EbookSnapshotServiceImpl(EbookSnapshotMapper ebookSnapshotMapper) {
        this.ebookSnapshotMapper = ebookSnapshotMapper;
    }

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

    @Override
    public List<DailyStatResp> get30Statistic() {
        String startDate = LocalDate.now().minusDays(29).toString();
        return ebookSnapshotMapper.selectLast30Days(startDate);
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
