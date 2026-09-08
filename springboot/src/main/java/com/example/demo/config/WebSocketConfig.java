package com.example.demo.config;

import com.example.demo.websocket.NotifyWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置：通知端点 /ws/{token}
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotifyWebSocketHandler notifyWebSocketHandler;

    public WebSocketConfig(NotifyWebSocketHandler notifyWebSocketHandler) {
        this.notifyWebSocketHandler = notifyWebSocketHandler;
    }

    /**
     * 注册 WebSocket 通知端点：/ws/{token}，token 用于标识连接用户
     * setAllowedOrigins("*") 允许所有来源连接（开发环境，生产环境建议收紧）
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notifyWebSocketHandler, "/ws/{token}").setAllowedOrigins("*");
    }
}
