package com.example.demo.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 通知处理器：维护在线连接，支持广播通知消息
 */
@Slf4j
@Component
public class NotifyWebSocketHandler extends TextWebSocketHandler {

    // 在线连接池：sessionId → WebSocketSession，ConcurrentHashMap 保证并发安全
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 连接建立后回调：将 session 加入在线连接池
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket 连接建立: {}", session.getId());
    }

    /**
     * 连接关闭后回调：从在线连接池移除 session
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket 连接关闭: {}", session.getId());
    }

    /**
     * 传输异常回调：从在线连接池移除异常 session，防止泄漏
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session.getId());
        log.warn("WebSocket 连接异常: {}", session.getId(), exception);
    }

    /**
     * 向所有在线连接广播通知消息（遍历连接池，跳过已关闭的 session，单个失败不影响其他推送）
     */
    public void sendToAll(String message) {
        TextMessage text = new TextMessage(message);
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(text);
                }
            } catch (Exception e) {
                log.warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
    }
}
