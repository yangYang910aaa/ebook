package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.EbookReq;
import com.example.demo.dto.EbookResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.service.EbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 电子书管理控制器：提供电子书的分页/条件查询、新增/编辑、删除及封面图片上传接口。
 * 路径前缀 /ebook，删除电子书时级联清理其下文档、内容及快照数据。
 */
@Tag(name = "电子书管理")
@RestController
@RequestMapping("/ebook")
public class EbookController {

    private final EbookService ebookService;

    public EbookController(EbookService ebookService) {
        this.ebookService = ebookService;
    }

    /** GET /ebook/query — 电子书分页查询，支持按名称模糊筛选和二级分类精确筛选 */
    @Operation(summary = "电子书分页/条件查询")
    @GetMapping("/query")
    public Result<PageResult<EbookResp>> query(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) Long category2Id,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(ebookService.query(name, category2Id, new PageReq(pageNum, pageSize)));
    }

    /** POST /ebook/save — 新增或编辑电子书（校验二级分类与一级分类的归属关系） */
    @Operation(summary = "新增/编辑电子书")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody EbookReq req) {
        ebookService.save(req);
        return Result.success();
    }

    /** GET /ebook/remove?id= — 删除电子书，级联删除其下全部文档、内容及快照 */
    @Operation(summary = "删除电子书")
    @GetMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        ebookService.remove(id);
        return Result.success();
    }

    /** POST /ebook/uploadImage — 封面图片上传，校验格式（jpg/jpeg/gif/png）和大小（≤10MB），返回可访问 URL */
    @Operation(summary = "封面图片上传")
    @PostMapping("/uploadImage")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(ebookService.uploadImage(file));
    }
}
