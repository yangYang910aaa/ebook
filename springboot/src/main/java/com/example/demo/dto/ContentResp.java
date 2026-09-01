package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档内容响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentResp {
    private Long id;
    private String content;
}
