package com.example.demo.service;

import com.example.demo.dto.ContentResp;
import com.example.demo.dto.DocReq;
import com.example.demo.dto.DocResp;

import java.util.List;

/**
 * 文档服务接口：定义电子书章节（文档）的树形查询、内容读写、级联删除及点赞/取消点赞等业务操作。
 * 点赞基于客户端 IP + 文档 ID 的 Redis key 做防重。
 */
public interface DocService {

    /**
     * 查询某电子书下全部文档，组装为树形结构，并标记当前 IP 是否已点赞。
     * @param ebookId 电子书 ID
     * @param ip 客户端 IP（用于点赞状态标记）
     * @return 文档树根节点列表
     */
    List<DocResp> all(Long ebookId, String ip);

    /**
     * 获取文档富文本内容；count=true 时阅读数 +1（后台预览传 false 不计）。
     * @param id 文档 ID
     * @param count 是否计入阅读数
     * @return 文档内容响应
     */
    ContentResp findContent(Long id, boolean count);

    /**
     * 新增或编辑文档及其富文本内容，含父文档防环校验（不能将自身或后代设为父文档）。
     * @param req 文档请求参数
     */
    void save(DocReq req);

    /**
     * 级联删除文档（逗号分隔多个 id），同时删除其所有子文档及对应内容。
     * @param idsStr 逗号分隔的文档 ID 字符串
     */
    void delete(String idsStr);

    /**
     * 点赞文档，基于 IP + 文档 ID 的 Redis key 防重复点赞，成功后异步推送 WebSocket 通知。
     * @param id 文档 ID
     * @param ip 客户端 IP
     */
    void vote(Long id, String ip);

    /**
     * 取消点赞，删除 Redis 防重 key 并扣减点赞数。
     * @param id 文档 ID
     * @param ip 客户端 IP
     */
    void unvote(Long id, String ip);
}
