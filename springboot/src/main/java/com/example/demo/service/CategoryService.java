package com.example.demo.service;

import com.example.demo.dto.CategoryReq;
import com.example.demo.dto.CategoryResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.entity.Category;

import java.util.List;

/**
 * 分类服务接口：定义电子书分类的树形查询、一级分类查询、分页查询、新增/编辑及删除等业务操作。
 * 分类为两级树形结构（parent=0 为一级），删除前校验是否存在子分类或被电子书引用。
 */
public interface CategoryService {

    /**
     * 查询全部分类（扁平列表，前端组装为树形/级联选择器）。
     * @return 全部分类列表
     */
    List<Category> getCategoryList();

    /**
     * 查询所有一级分类（parent=0）。
     * @return 一级分类列表
     */
    List<Category> getParents();

    /**
     * 分类分页查询，支持按名称模糊筛选。
     * @param name 分类名称（可空）
     * @param pageReq 分页参数
     * @return 分页结果
     */
    PageResult<CategoryResp> list(String name, PageReq pageReq);

    /**
     * 新增或编辑分类；二级分类只能挂载到一级分类下，一级分类不可改父节点。
     * @param req 分类请求参数
     */
    void save(CategoryReq req);

    /**
     * 删除分类；存在子分类或已被电子书引用时拒绝删除。
     * @param id 分类 ID
     */
    void remove(Long id);
}
