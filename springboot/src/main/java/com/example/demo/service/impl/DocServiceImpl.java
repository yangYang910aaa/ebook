package com.example.demo.service.impl;

import com.example.demo.common.BusinessException;
import com.example.demo.dto.ContentResp;
import com.example.demo.dto.DocReq;
import com.example.demo.dto.DocResp;
import com.example.demo.entity.Content;
import com.example.demo.entity.Doc;
import com.example.demo.mapper.ContentMapper;
import com.example.demo.mapper.DocMapper;
import com.example.demo.service.DocService;
import com.example.demo.util.CopyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档服务实现
 */
@Service
public class DocServiceImpl implements DocService {

    private final DocMapper docMapper;
    private final ContentMapper contentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public DocServiceImpl(DocMapper docMapper, ContentMapper contentMapper, RedisTemplate<String, Object> redisTemplate) {
        this.docMapper = docMapper;
        this.contentMapper = contentMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<DocResp> all(Long ebookId) {
        List<Doc> docs = docMapper.selectByEbookId(ebookId);
        Map<Long, DocResp> map = new HashMap<>();
        for (Doc doc : docs) {
            DocResp resp = CopyUtil.copy(doc, DocResp.class);
            map.put(resp.getId(), resp);
        }
        List<DocResp> roots = new ArrayList<>();
        for (DocResp resp : map.values()) {
            DocResp parent = map.get(resp.getParent());
            if (parent != null) {
                parent.getChildren().add(resp);
            } else {
                roots.add(resp);
            }
        }
        return roots;
    }

    @Override
    public ContentResp findContent(Long id) {
        Content content = contentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException("文档内容不存在");
        }
        // 打开文档阅读数 +1
        docMapper.incrementView(id);
        return new ContentResp(content.getId(), content.getContent());
    }

    @Override
    @Transactional
    public void save(DocReq req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BusinessException("文档名称不能为空");
        }
        if (req.getEbookId() == null) {
            throw new BusinessException("所属电子书不能为空");
        }
        Doc doc = new Doc();
        doc.setEbookId(req.getEbookId());
        doc.setParent(req.getParent() == null ? 0L : req.getParent());
        doc.setName(req.getName());
        doc.setSort(req.getSort() == null ? 0 : req.getSort());
        if (req.getId() == null) {
            docMapper.insert(doc);
            Content content = new Content();
            content.setId(doc.getId());
            content.setContent(req.getContent());
            contentMapper.insert(content);
        } else {
            doc.setId(req.getId());
            docMapper.update(doc);
            Content content = new Content();
            content.setId(req.getId());
            content.setContent(req.getContent());
            if (contentMapper.update(content) == 0) {
                contentMapper.insert(content);
            }
        }
    }

    @Override
    @Transactional
    public void delete(String idsStr) {
        List<Long> allIds = new ArrayList<>();
        for (String s : idsStr.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                collectIds(Long.valueOf(trimmed), allIds);
            }
        }
        if (!allIds.isEmpty()) {
            docMapper.deleteByIds(allIds);
            contentMapper.deleteByIds(allIds);
        }
    }

    private void collectIds(Long id, List<Long> result) {
        result.add(id);
        List<Doc> children = docMapper.selectByParent(id);
        for (Doc child : children) {
            collectIds(child.getId(), result);
        }
    }

    @Override
    public void vote(Long id, String ip) {
        String key = "vote:" + ip + ":" + id;
        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1");
        if (Boolean.FALSE.equals(first)) {
            throw new BusinessException("您已点赞过");
        }
        docMapper.incrementVote(id);
        // WebSocket 点赞通知（阶段 7 接入）
    }
}
