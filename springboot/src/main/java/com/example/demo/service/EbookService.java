package com.example.demo.service;

import com.example.demo.dto.EbookReq;
import com.example.demo.dto.EbookResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 电子书服务
 */
public interface EbookService {

    PageResult<EbookResp> query(String name, Long category2Id, PageReq pageReq);

    void save(EbookReq req);

    void remove(Long id);

    String uploadImage(MultipartFile file);
}
