package com.example.demo.service;

import com.example.demo.dto.ContentResp;
import com.example.demo.dto.DocReq;
import com.example.demo.dto.DocResp;

import java.util.List;

/**
 * 文档服务
 */
public interface DocService {

    List<DocResp> all(Long ebookId);

    ContentResp findContent(Long id);

    void save(DocReq req);

    void delete(String idsStr);

    void vote(Long id, String ip);
}
