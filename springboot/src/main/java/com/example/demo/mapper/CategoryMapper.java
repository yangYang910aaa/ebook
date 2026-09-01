package com.example.demo.mapper;

import com.example.demo.dto.CategoryResp;
import com.example.demo.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类 Mapper
 */
@Mapper
public interface CategoryMapper {

    List<Category> selectAll();

    List<Category> selectByParent(@Param("parent") Long parent);

    Category selectById(@Param("id") Long id);

    long count(@Param("name") String name);

    List<CategoryResp> selectPage(@Param("name") String name,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    int insert(Category category);

    int update(Category category);

    int deleteById(@Param("id") Long id);
}
