package com.example.demo.mapper;

import com.example.demo.dto.EbookResp;
import com.example.demo.entity.Ebook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电子书 Mapper：电子书表的 CRUD、分页查询及定时聚合统计。
 * SQL 见 resources/mapper/EbookMapper.xml。
 */
@Mapper
public interface EbookMapper {

    /** 按名称模糊和二级分类精确统计电子书数量 */
    long count(@Param("name") String name, @Param("category2Id") Long category2Id);

    /** 引用该分类（一级或二级）的电子书数量，用于删除分类前的引用校验 */
    long countByCategory(@Param("id") Long id);

    /** 按名称模糊和二级分类精确分页查询电子书 */
    List<EbookResp> selectPage(@Param("name") String name,
                               @Param("category2Id") Long category2Id,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    /** 按 ID 查询电子书 */
    Ebook selectById(@Param("id") Long id);

    /** 新增电子书 */
    int insert(Ebook ebook);

    /** 更新电子书 */
    int update(Ebook ebook);

    /** 按 ID 删除电子书 */
    int deleteById(@Param("id") Long id);

    /** 定时聚合：按文档汇总各电子书的文档数/阅读数/点赞数，回写 ebook 表 */
    int aggregateStats();
}
