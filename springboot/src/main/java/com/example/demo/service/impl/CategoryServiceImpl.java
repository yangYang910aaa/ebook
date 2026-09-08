package com.example.demo.service.impl;

import com.example.demo.common.BusinessException;
import com.example.demo.dto.CategoryReq;
import com.example.demo.dto.CategoryResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.entity.Category;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.EbookMapper;
import com.example.demo.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现：负责电子书分类的查询、新增/编辑及删除。
 * 分类为两级树形结构（parent=0 为一级），核心约束：
 * 1. 二级分类只能挂载到一级分类下（父分类的 parent 必须为 0）；
 * 2. 一级分类不可修改父分类（保持层级稳定）；
 * 3. 删除前校验：存在子分类或已被电子书引用时拒绝删除。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final EbookMapper ebookMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, EbookMapper ebookMapper) {
        this.categoryMapper = categoryMapper;
        this.ebookMapper = ebookMapper;
    }

    /** 查询全部分类（扁平列表，由前端组装为树形/级联选择器） */
    @Override
    public List<Category> getCategoryList() {
        return categoryMapper.selectAll();
    }

    /** 查询所有一级分类（parent=0） */
    @Override
    public List<Category> getParents() {
        return categoryMapper.selectByParent(0L);
    }

    /** 分类分页查询：修正非法分页参数，pageSize 上限 1000，按名称模糊筛选 */
    @Override
    public PageResult<CategoryResp> list(String name, PageReq pageReq) {
        int pageNum = pageReq.getPageNum() == null || pageReq.getPageNum() < 1 ? 1 : pageReq.getPageNum();
        int pageSize = pageReq.getPageSize() == null || pageReq.getPageSize() < 1 ? 10 : pageReq.getPageSize();
        pageSize = Math.min(pageSize, 1000);
        long total = categoryMapper.count(name);
        List<CategoryResp> list = categoryMapper.selectPage(name, (pageNum - 1) * pageSize, pageSize);
        return new PageResult<>(total, list);
    }

    /**
     * 新增或编辑分类：
     * - 新增：parent 默认为 0（一级），若指定父分类则校验其存在且必须是一级分类（parent=0）；
     * - 编辑：一级分类不可修改父分类（保持层级稳定），二级分类可改父节点但仍须挂载到一级分类下。
     */
    @Override
    public void save(CategoryReq req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BusinessException("分类名称不能为空");
        }
        long parent = req.getParent() == null ? 0L : req.getParent();
        if (parent != 0) {
            Category parentCategory = categoryMapper.selectById(parent);
            if (parentCategory == null) {
                throw new BusinessException("父分类不存在");
            }
            if (parentCategory.getParent().longValue() != 0) {
                throw new BusinessException("只能挂载到一级分类下");
            }
        }
        if (req.getId() == null) {
            Category category = new Category();
            category.setParent(parent);
            category.setName(req.getName());
            category.setSort(req.getSort() == null ? 0 : req.getSort());
            categoryMapper.insert(category);
        } else {
            Category exist = categoryMapper.selectById(req.getId());
            if (exist == null) {
                throw new BusinessException("分类不存在");
            }
            Category category = new Category();
            category.setId(req.getId());
            category.setName(req.getName());
            category.setSort(req.getSort() == null ? exist.getSort() : req.getSort());
            // 一级分类不可修改父分类
            category.setParent(exist.getParent() == 0L ? 0L : (req.getParent() == null ? exist.getParent() : req.getParent()));
            categoryMapper.update(category);
        }
    }

    /**
     * 删除分类：存在子分类或已被电子书引用时拒绝删除，避免孤儿数据和引用失效。
     */
    @Override
    public void remove(Long id) {
        if (categoryMapper.countByParent(id) > 0) {
            throw new BusinessException("该分类下存在子分类，无法删除");
        }
        if (ebookMapper.countByCategory(id) > 0) {
            throw new BusinessException("该分类已被电子书使用，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
