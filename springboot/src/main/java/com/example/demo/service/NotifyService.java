package com.example.demo.service;

import com.example.demo.websocket.NotifyWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知服务：将点赞等业务事件通过 WebSocket 异步推送给所有在线客户端，与主业务流程解耦。
 * 使用 @Async 异步执行，避免通知发送阻塞业务线程；发送失败仅记录日志，不影响主流程。
 */
@Slf4j
@Service
public class NotifyService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final NotifyWebSocketHandler webSocketHandler;

    public NotifyService(NotifyWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * 点赞通知（异步线程执行，自动继承日志流水号）
     */
    @Async
    public void notifyVote(String docName, String ip) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "用户 " + ip + " 点赞了文档《" + docName + "》");
            webSocketHandler.sendToAll(OBJECT_MAPPER.writeValueAsString(body));
        } catch (Exception e) {
            log.warn("点赞通知发送失败: {}", e.getMessage());
        }
    }
}
