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
 * 分类管理控制器：提供电子书分类的树形查询、一级分类查询、分页查询、新增/编辑及删除接口。
 * 路径前缀 /category，分类为两级树形结构（parent=0 为一级），删除前校验是否存在子分类或被电子书引用。
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** GET /category/getCategoryList — 查询全部分类（扁平列表，前端组装为树形/级联选择器） */
    @Operation(summary = "全部分类（树形/级联选择用）")
    @GetMapping("/getCategoryList")
    public Result<List<Category>> getCategoryList() {
        return Result.success(categoryService.getCategoryList());
    }

    /** GET /category/getParents — 查询所有一级分类（parent=0） */
    @Operation(summary = "一级分类")
    @GetMapping("/getParents")
    public Result<List<Category>> getParents() {
        return Result.success(categoryService.getParents());
    }

    /** GET /category/list — 分类分页查询，支持按名称模糊筛选 */
    @Operation(summary = "分类分页查询")
    @GetMapping("/list")
    public Result<PageResult<CategoryResp>> list(@RequestParam(required = false) String name,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(categoryService.list(name, new PageReq(pageNum, pageSize)));
    }

    /** POST /category/save — 新增或编辑分类；二级分类只能挂载到一级分类下，一级分类不可改父节点 */
    @Operation(summary = "新增/编辑分类")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody CategoryReq req) {
        categoryService.save(req);
        return Result.success();
    }

    /** GET /category/remove?id= — 删除分类；存在子分类或已被电子书引用时拒绝删除 */
    @Operation(summary = "删除分类")
    @GetMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        categoryService.remove(id);
        return Result.success();
    }
}
