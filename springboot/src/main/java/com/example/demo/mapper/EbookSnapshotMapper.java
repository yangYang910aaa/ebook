package com.example.demo.mapper;

import com.example.demo.dto.DailyStatResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 电子书快照 Mapper
 */
@Mapper
public interface EbookSnapshotMapper {

    /** 为所有电子书生成当天快照（已存在则不插入），增量对比昨日 */
    int insertDailySnapshot();

    /** 今日快照聚合：view_count / vote_count 总和 */
    Map<String, Object> selectTodayStat();

    /** 昨日快照聚合：view_count / vote_count 总和 */
    Map<String, Object> selectYesterdayStat();

    /** ebook 表总量：总阅读 / 总点赞 */
    Map<String, Object> selectTotalStat();

    /** 近 30 天日增量（按日期聚合） */
    List<DailyStatResp> selectLast30Days(@Param("startDate") String startDate);
}
