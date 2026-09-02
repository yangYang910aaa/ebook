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

    /** 最近一次快照（基线）：max_date / view_count / vote_count */
    Map<String, Object> selectLatestSnapshotStat();

    /** 最近第二次快照：view_count / vote_count */
    Map<String, Object> selectPrevSnapshotStat();

    /** ebook 表总量：总阅读 / 总点赞 */
    Map<String, Object> selectTotalStat();

    /** 近 30 天日增量（按日期聚合） */
    List<DailyStatResp> selectLast30Days(@Param("startDate") String startDate);

    /** 删除某电子书的全部快照 */
    int deleteByEbookId(@Param("ebookId") Long ebookId);
}
