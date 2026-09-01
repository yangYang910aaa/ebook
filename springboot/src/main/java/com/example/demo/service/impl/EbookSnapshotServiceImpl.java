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
        Map<String, Object> today = ebookSnapshotMapper.selectTodayStat();
        Map<String, Object> yesterday = ebookSnapshotMapper.selectYesterdayStat();

        long totalView = toLong(total.get("view_count"));
        long totalVote = toLong(total.get("vote_count"));
        long todayView = toLong(today.get("view_count"));
        long todayVote = toLong(today.get("vote_count"));
        long yesterdayView = toLong(yesterday.get("view_count"));

        // 今日快照未生成时回退到 ebook 实时总量，保证首页始终有数据
        if (todayView == 0 && totalView > 0) {
            todayView = totalView;
            todayVote = totalVote;
        }

        double voteRate = totalView == 0 ? 0.0 : round2(totalVote * 100.0 / totalView);

        // 预计今日阅读：按已过时间占全天比例推算
        int minutes = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        int elapsed = Math.max(minutes, 1);
        long estimatedToday = todayView * 1440L / elapsed;

        // 预计今日阅读增长率（对比昨日）
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
