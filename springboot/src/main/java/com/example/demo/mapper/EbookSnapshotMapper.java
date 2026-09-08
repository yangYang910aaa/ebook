package com.example.demo.mapper;

import com.example.demo.dto.DailyStatResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 电子书快照 Mapper：每日快照表的增量生成、统计基线查询及近 30 天趋势查询。
 * 快照由定时任务每日生成，记录各电子书当天的累计阅读/点赞及日增量。
 * SQL 见 resources/mapper/EbookSnapshotMapper.xml。
 */
@Mapper
public interface EbookSnapshotMapper {

    /** 为所有电子书生成当天快照（已存在则不插入），增量对比昨日快照计算 viewIncrease/voteIncrease */
    int insertDailySnapshot();

    /** 最近一次快照（基线）：max_date / view_count / vote_count，用于计算今日增量 */
    Map<String, Object> selectLatestSnapshotStat();

    /** 最近第二次快照：view_count / vote_count，用于计算昨日增量 */
    Map<String, Object> selectPrevSnapshotStat();

    /** ebook 表总量：总阅读 / 总点赞，实时累计值 */
    Map<String, Object> selectTotalStat();

    /** 近 30 天日增量（按日期聚合），startDate 为起始日期（含） */
    List<DailyStatResp> selectLast30Days(@Param("startDate") String startDate);

    /** 删除某电子书的全部快照（级联删除电子书时使用） */
    int deleteByEbookId(@Param("ebookId") Long ebookId);
}
