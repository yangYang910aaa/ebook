package com.example.demo.service;

import com.example.demo.dto.EbookReq;
import com.example.demo.dto.EbookResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 电子书服务接口：定义电子书的分页/条件查询、新增/编辑、删除及封面图片上传等业务操作。
 * 删除电子书时级联清理其下文档、内容及快照数据。
 */
public interface EbookService {

    /**
     * 电子书分页查询，支持按名称模糊筛选和二级分类精确筛选。
     * @param name 电子书名称（可空）
     * @param category2Id 二级分类 ID（可空）
     * @param pageReq 分页参数
     * @return 分页结果
     */
    PageResult<EbookResp> query(String name, Long category2Id, PageReq pageReq);

    /**
     * 新增或编辑电子书，校验二级分类与一级分类的归属关系。
     * @param req 电子书请求参数
     */
    void save(EbookReq req);

    /**
     * 删除电子书，级联删除其下全部文档、内容及快照。
     * @param id 电子书 ID
     */
    void remove(Long id);

    /**
     * 封面图片上传，校验格式（jpg/jpeg/gif/png）和大小（≤10MB），返回可访问 URL。
     * @param file 上传的图片文件
     * @return 图片可访问 URL
     */
    String uploadImage(MultipartFile file);
}
