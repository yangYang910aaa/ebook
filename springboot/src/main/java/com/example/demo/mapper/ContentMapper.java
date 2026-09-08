package com.example.demo.mapper;

import com.example.demo.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档内容 Mapper：文档富文本内容表的 CRUD，content.id 与 doc.id 一一对应。
 * SQL 见 resources/mapper/ContentMapper.xml。
 */
@Mapper
public interface ContentMapper {

    /** 按 ID 查询文档内容 */
    Content selectById(@Param("id") Long id);

    /** 新增文档内容 */
    int insert(Content content);

    /** 更新文档内容 */
    int update(Content content);

    /** 按 ID 列表批量删除文档内容（级联删除时使用） */
    int deleteByIds(@Param("ids") List<Long> ids);

    /** 删除某电子书下全部文档的内容（content.id = doc.id，通过 doc.ebookId 关联） */
    int deleteByEbookId(@Param("ebookId") Long ebookId);
}
