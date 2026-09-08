package com.example.demo.mapper;

import com.example.demo.dto.CategoryResp;
import com.example.demo.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类 Mapper：分类表的 CRUD、分页查询及子分类数量统计。
 * SQL 见 resources/mapper/CategoryMapper.xml。
 */
@Mapper
public interface CategoryMapper {

    /** 查询全部分类（扁平列表） */
    List<Category> selectAll();

    /** 按父分类 ID 查询子分类列表 */
    List<Category> selectByParent(@Param("parent") Long parent);

    /** 某分类下的子分类数量，用于删除前校验 */
    long countByParent(@Param("parent") Long parent);

    /** 按 ID 查询分类 */
    Category selectById(@Param("id") Long id);

    /** 按名称模糊统计分类数量 */
    long count(@Param("name") String name);

    /** 按名称模糊分页查询分类 */
    List<CategoryResp> selectPage(@Param("name") String name,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    /** 新增分类 */
    int insert(Category category);

    /** 更新分类 */
    int update(Category category);

    /** 按 ID 删除分类 */
    int deleteById(@Param("id") Long id);
}
