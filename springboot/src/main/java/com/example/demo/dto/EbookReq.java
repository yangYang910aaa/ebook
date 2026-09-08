package com.example.demo.dto;

import lombok.Data;

/**
 * 电子书新增/编辑请求
 */
@Data
public class EbookReq {
    // 电子书ID（新增时为空，编辑时必填）
    private Long id;
    // 电子书名称
    private String name;
    // 一级分类ID
    private Long category1Id;
    // 二级分类ID
    private Long category2Id;
    // 电子书描述
    private String description;
    // 封面图片URL（上传后返回的访问路径）
    private String cover;
}
