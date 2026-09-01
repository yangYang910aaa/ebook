package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CategoryReq;
import com.example.demo.dto.CategoryResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.entity.Category;
import com.example.demo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类管理
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "全部分类（树形/级联选择用）")
    @GetMapping("/getCategoryList")
    public Result<List<Category>> getCategoryList() {
        return Result.success(categoryService.getCategoryList());
    }

    @Operation(summary = "一级分类")
    @GetMapping("/getParents")
    public Result<List<Category>> getParents() {
        return Result.success(categoryService.getParents());
    }

    @Operation(summary = "分类分页查询")
    @GetMapping("/list")
    public Result<PageResult<CategoryResp>> list(@RequestParam(required = false) String name,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(categoryService.list(name, new PageReq(pageNum, pageSize)));
    }

    @Operation(summary = "新增/编辑分类")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody CategoryReq req) {
        categoryService.save(req);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @GetMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        categoryService.remove(id);
        return Result.success();
    }
}
