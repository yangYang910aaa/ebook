package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
