package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页请求：所有分页查询接口的入参基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageReq {
    // 页码（从1开始，默认1）
    private Integer pageNum = 1;
    // 每页条数（默认10）
    private Integer pageSize = 10;
}
