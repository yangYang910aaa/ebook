package com.example.demo.dto;

import lombok.Data;

/**
 * 文档保存请求（含富文本内容）：新增/编辑文档目录节点及正文
 */
@Data
public class DocReq {
    // 文档ID（新增时为空，编辑时必填）
    private Long id;
    // 所属电子书ID
    private Long ebookId;
    // 父文档ID（0表示根节点）
    private Long parent;
    // 文档名称
    private String name;
    // 同级排序号
    private Integer sort;
    // 富文本内容（HTML，仅编辑正文时传入）
    private String content;
}
