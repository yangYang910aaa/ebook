package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ContentResp;
import com.example.demo.dto.DocReq;
import com.example.demo.dto.DocResp;
import com.example.demo.service.DocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文档管理
 */
@Tag(name = "文档管理")
@RestController
@RequestMapping("/doc")
public class DocController {

    private final DocService docService;

    public DocController(DocService docService) {
        this.docService = docService;
    }

    @Operation(summary = "某电子书全部文档（树）")
    @GetMapping("/all")
    public Result<List<DocResp>> all(@RequestParam Long ebookId) {
        return Result.success(docService.all(ebookId));
    }

    @Operation(summary = "获取文档富文本内容")
    @GetMapping("/find-content/{id}")
    public Result<ContentResp> findContent(@PathVariable Long id) {
        return Result.success(docService.findContent(id));
    }

    @Operation(summary = "保存文档及内容")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody DocReq req) {
        docService.save(req);
        return Result.success();
    }

    @Operation(summary = "级联删除文档（逗号分隔 id）")
    @DeleteMapping("/delete/{idsStr}")
    public Result<Void> delete(@PathVariable String idsStr) {
        docService.delete(idsStr);
        return Result.success();
    }

    @Operation(summary = "点赞")
    @GetMapping("/vote/{id}")
    public Result<Void> vote(@PathVariable Long id, HttpServletRequest request) {
        docService.vote(id, request.getRemoteAddr());
        return Result.success();
    }
}
