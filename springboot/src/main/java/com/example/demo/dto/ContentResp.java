package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档内容响应：查看文档正文时返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentResp {
    // 文档ID
    private Long id;
    // 文档富文本内容（HTML）
    private String content;
}
