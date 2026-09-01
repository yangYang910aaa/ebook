package com.example.demo.service;

import com.example.demo.dto.CategoryReq;
import com.example.demo.dto.CategoryResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.entity.Category;

import java.util.List;

/**
 * 分类服务
 */
public interface CategoryService {

    List<Category> getCategoryList();

    List<Category> getParents();

    PageResult<CategoryResp> list(String name, PageReq pageReq);

    void save(CategoryReq req);

    void remove(Long id);
}
