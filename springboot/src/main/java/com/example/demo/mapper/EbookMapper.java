package com.example.demo.mapper;

import com.example.demo.dto.EbookResp;
import com.example.demo.entity.Ebook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电子书 Mapper
 */
@Mapper
public interface EbookMapper {

    long count(@Param("name") String name, @Param("category2Id") Long category2Id);

    List<EbookResp> selectPage(@Param("name") String name,
                               @Param("category2Id") Long category2Id,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    Ebook selectById(@Param("id") Long id);

    int insert(Ebook ebook);

    int update(Ebook ebook);

    int deleteById(@Param("id") Long id);

    int updateStats(@Param("id") Long id,
                    @Param("docCount") int docCount,
                    @Param("viewCount") int viewCount,
                    @Param("voteCount") int voteCount);
}
