package com.example.demo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档响应（树节点）：电子书目录的树形结构节点
 */
@Data
public class DocResp {
    // 文档ID
    private Long id;
    // 所属电子书ID
    private Long ebookId;
    // 父文档ID（0表示根节点）
    private Long parent;
    // 文档名称
    private String name;
    // 同级排序号（升序）
    private Integer sort;
    // 阅读数
    private Integer viewCount;
    // 点赞数
    private Integer voteCount;
    // 当前登录用户是否已点赞
    private Boolean liked;
    // 子文档列表（递归树形结构）
    private List<DocResp> children = new ArrayList<>();
}
