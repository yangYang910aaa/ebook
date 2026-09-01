package com.example.demo.dto;

import lombok.Data;

/**
 * 电子书新增/编辑请求
 */
@Data
public class EbookReq {
    private Long id;
    private String name;
    private Long category1Id;
    private Long category2Id;
    private String description;
    private String cover;
}
