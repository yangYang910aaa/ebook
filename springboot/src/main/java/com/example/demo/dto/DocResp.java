package com.example.demo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档响应（树节点）
 */
@Data
public class DocResp {
    private Long id;
    private Long ebookId;
    private Long parent;
    private String name;
    private Integer sort;
    private Integer viewCount;
    private Integer voteCount;
    private List<DocResp> children = new ArrayList<>();
}
