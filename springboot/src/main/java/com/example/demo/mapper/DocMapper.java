package com.example.demo.mapper;

import com.example.demo.entity.Doc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档 Mapper：文档（章节）表的 CRUD、分页查询、级联删除及阅读/点赞计数原子增减。
 * SQL 见 resources/mapper/DocMapper.xml。
 */
@Mapper
public interface DocMapper {

    /** 按电子书 ID 查询其下全部文档（扁平列表） */
    List<Doc> selectByEbookId(@Param("ebookId") Long ebookId);

    /** 按父文档 ID 查询子文档列表 */
    List<Doc> selectByParent(@Param("parent") Long parent);

    /** 按 ID 查询文档 */
    Doc selectById(@Param("id") Long id);

    /** 新增文档 */
    int insert(Doc doc);

    /** 更新文档 */
    int update(Doc doc);

    /** 按 ID 列表批量删除文档（级联删除时使用） */
    int deleteByIds(@Param("ids") List<Long> ids);

    /** 删除某电子书下的全部文档 */
    int deleteByEbookId(@Param("ebookId") Long ebookId);

    /** 阅读数 +1（原子操作，避免并发计数丢失） */
    int incrementView(@Param("id") Long id);

    /** 点赞数 +1（原子操作） */
    int incrementVote(@Param("id") Long id);

    /** 点赞数 -1（取消点赞时使用，原子操作） */
    int decrementVote(@Param("id") Long id);
}
