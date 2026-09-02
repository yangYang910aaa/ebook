package com.example.demo.mapper;

import com.example.demo.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档内容 Mapper
 */
@Mapper
public interface ContentMapper {

    Content selectById(@Param("id") Long id);

    int insert(Content content);

    int update(Content content);

    int deleteByIds(@Param("ids") List<Long> ids);

    /** 删除某电子书下全部文档的内容（content.id = doc.id） */
    int deleteByEbookId(@Param("ebookId") Long ebookId);
}
