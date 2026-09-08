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
 * 文档服务实现：负责电子书章节（文档）的树形组装、内容读写、级联删除及点赞/取消点赞。
 * 核心逻辑点：
 * 1. 文档树形组装：扁平列表按 parent 指针构建树，并按 sort 递归排序；
 * 2. 父文档防环校验：编辑时禁止将自身或后代设为父文档；
 * 3. 级联删除：递归收集所有后代文档 ID，同时删除文档与内容；
 * 4. 点赞防重：基于 "vote:{ip}:{docId}" 的 Redis key，setIfAbsent 保证同一 IP 不可重复点赞，TTL 180 天。
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

    /**
     * 查询某电子书下全部文档并组装为树形结构：
     * 1. 查扁平列表，逐个标记当前 IP 是否已点赞（查 Redis key 是否存在）；
     * 2. 按 parent 指针挂到父节点的 children，无父节点的作为根；
     * 3. HashMap 遍历顺序不确定，构建树后按 sort 递归排序保证目录顺序。
     */
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

    /** 递归按 sort 字段对文档树排序，null 视为 0 */
    private void sortTree(List<DocResp> nodes) {
        nodes.sort(Comparator.comparingInt(d -> d.getSort() == null ? 0 : d.getSort()));
        for (DocResp node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    /**
     * 获取文档富文本内容；count=true 时阅读数 +1（前台阅读计），
     * 后台编辑/预览传 count=false 不计入阅读量。
     */
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

    /**
     * 新增或编辑文档及内容（事务）：
     * 1. 校验名称、所属电子书非空；
     * 2. 父文档校验：存在性、同属一个电子书、编辑时防环（不能将自身或后代设为父文档）；
     * 3. 新增时同时插入 doc 和 content（content.id = doc.id）；
     * 4. 编辑时更新 doc，content 存在则更新、不存在则插入（兼容历史数据）。
     */
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

    /**
     * 级联删除文档（事务）：对逗号分隔的每个 ID 递归收集其所有后代文档 ID，
     * 然后批量删除文档记录和对应的内容记录（content.id = doc.id）。
     */
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

    /** 递归收集指定文档及其所有后代文档的 ID，用于级联删除和防环校验 */
    private void collectIds(Long id, List<Long> result) {
        result.add(id);
        List<Doc> children = docMapper.selectByParent(id);
        for (Doc child : children) {
            collectIds(child.getId(), result);
        }
    }

    /**
     * 点赞文档：
     * 1. 校验文档存在；
     * 2. Redis setIfAbsent 写入 "vote:{ip}:{id}"（TTL 180 天），写入失败说明已点赞过，抛异常；
     * 3. 写入成功则数据库点赞数 +1，并异步推送 WebSocket 点赞通知。
     */
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

    /**
     * 取消点赞：
     * 1. 校验文档存在；
     * 2. 删除 Redis 防重 key，删除失败说明尚未点赞，抛异常；
     * 3. 删除成功则数据库点赞数 -1。
     */
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

    /** 构造点赞防重 Redis key：vote:{客户端IP}:{文档ID} */
    private String voteKey(Long id, String ip) {
        return "vote:" + ip + ":" + id;
    }
}
