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
import com.example.demo.service.NotifyService;
import com.example.demo.util.CopyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final NotifyService notifyService;

    public DocServiceImpl(DocMapper docMapper, ContentMapper contentMapper,
                          RedisTemplate<String, Object> redisTemplate, NotifyService notifyService) {
        this.docMapper = docMapper;
        this.contentMapper = contentMapper;
        this.redisTemplate = redisTemplate;
        this.notifyService = notifyService;
    }

    @Override
    public List<DocResp> all(Long ebookId, String ip) {
        List<Doc> docs = docMapper.selectByEbookId(ebookId);
        Map<Long, DocResp> map = new HashMap<>();
        for (Doc doc : docs) {
            DocResp resp = CopyUtil.copy(doc, DocResp.class);
            resp.setLiked(Boolean.TRUE.equals(redisTemplate.hasKey(voteKey(doc.getId(), ip))));
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
        // HashMap 遍历顺序不确定，构建树后需按 sort 递归排序，保证文档目录顺序正确
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<DocResp> nodes) {
        nodes.sort(Comparator.comparingInt(DocResp::getSort));
        for (DocResp node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    @Override
    public ContentResp findContent(Long id, boolean count) {
        Content content = contentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException("文档内容不存在");
        }
        // 打开文档阅读数 +1（前台阅读计；后台编辑/预览传 count=false 不计）
        if (count) {
            docMapper.incrementView(id);
        }
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
        long parent = req.getParent() == null ? 0L : req.getParent();
        if (parent != 0) {
            Doc parentDoc = docMapper.selectById(parent);
            if (parentDoc == null) {
                throw new BusinessException("父文档不存在");
            }
            if (!parentDoc.getEbookId().equals(req.getEbookId())) {
                throw new BusinessException("父文档必须属于同一电子书");
            }
            if (req.getId() != null) {
                if (parent == req.getId()) {
                    throw new BusinessException("不能将自身设为父文档");
                }
                List<Long> descendantIds = new ArrayList<>();
                collectIds(req.getId(), descendantIds);
                if (descendantIds.contains(parent)) {
                    throw new BusinessException("不能将子文档设为父文档");
                }
            }
        }
        Doc doc = new Doc();
        doc.setEbookId(req.getEbookId());
        doc.setParent(parent);
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
        Doc doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }
        String key = voteKey(id, ip);
        // TTL 半年：既保证"同一用户不可重复点赞"的语义，又避免 key 无限膨胀
        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofDays(180));
        if (Boolean.FALSE.equals(first)) {
            // 需求文档：同一用户不可重复点赞，重复点赞给出提示，不重复计数
            throw new BusinessException("您已点赞过");
        }
        docMapper.incrementVote(id);
        notifyService.notifyVote(doc.getName(), ip);
    }

    @Override
    public void unvote(Long id, String ip) {
        Doc doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }
        String key = voteKey(id, ip);
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.FALSE.equals(deleted)) {
            // 尚未点赞，无需取消
            throw new BusinessException("您尚未点赞");
        }
        docMapper.decrementVote(id);
    }

    private String voteKey(Long id, String ip) {
        return "vote:" + ip + ":" + id;
    }
}
