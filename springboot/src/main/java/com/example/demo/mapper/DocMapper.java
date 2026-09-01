package com.example.demo.mapper;

import com.example.demo.entity.Doc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档 Mapper
 */
@Mapper
public interface DocMapper {

    List<Doc> selectByEbookId(@Param("ebookId") Long ebookId);

    List<Doc> selectByParent(@Param("parent") Long parent);

    Doc selectById(@Param("id") Long id);

    int insert(Doc doc);

    int update(Doc doc);

    int deleteByIds(@Param("ids") List<Long> ids);

    int incrementView(@Param("id") Long id);

    int incrementVote(@Param("id") Long id);
}
