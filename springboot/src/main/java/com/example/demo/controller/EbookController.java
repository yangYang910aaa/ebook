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
 * 电子书管理
 */
@Tag(name = "电子书管理")
@RestController
@RequestMapping("/ebook")
public class EbookController {

    private final EbookService ebookService;

    public EbookController(EbookService ebookService) {
        this.ebookService = ebookService;
    }

    @Operation(summary = "电子书分页/条件查询")
    @GetMapping("/query")
    public Result<PageResult<EbookResp>> query(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) Long category2Id,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(ebookService.query(name, category2Id, new PageReq(pageNum, pageSize)));
    }

    @Operation(summary = "新增/编辑电子书")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody EbookReq req) {
        ebookService.save(req);
        return Result.success();
    }

    @Operation(summary = "删除电子书")
    @GetMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        ebookService.remove(id);
        return Result.success();
    }

    @Operation(summary = "封面图片上传")
    @PostMapping("/uploadImage")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(ebookService.uploadImage(file));
    }
}
