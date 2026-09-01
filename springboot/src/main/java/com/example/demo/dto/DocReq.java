package com.example.demo.dto;

import lombok.Data;

/**
 * 文档保存请求（含富文本内容）
 */
@Data
public class DocReq {
    private Long id;
    private Long ebookId;
    private Long parent;
    private String name;
    private Integer sort;
    private String content;
}
