package com.lcp.spb.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

/**
 * WebSocket 配置类
 * 
 * <p>配置 WebSocket 相关的 Bean，用于支持实时推送交易汇总数据。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>注册 WebSocket 处理器：将交易汇总 WebSocket 处理器映射到 /ws/trade-summary 路径</li>
 *   <li>配置 WebSocket 适配器：提供 WebSocket 请求处理支持</li>
 * </ul>
 * 
 * <p>WebSocket 路径：/ws/trade-summary
 * 
 * @author lcp
 */
@Configuration
public class WebSocketConfig {

    /**
     * 配置 WebSocket 路径映射
     * 
     * <p>将交易汇总 WebSocket 处理器注册到 /ws/trade-summary 路径。
     * 设置优先级为 -1，确保 WebSocket 映射优先于其他 HTTP 处理器。
     * 
     * @param handler 交易汇总 WebSocket 处理器
     * @return HandlerMapping 对象，包含 WebSocket 路径映射配置
     */
    @Bean
    public HandlerMapping webSocketMapping (TradeSummaryWebSocketHandler handler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/trade-summary", handler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(-1);
        return mapping;
    }

    /**
     * 配置 WebSocket 处理器适配器
     * 
     * <p>提供 WebSocket 请求处理支持，用于将 WebSocket 请求路由到相应的处理器。
     * 
     * @return WebSocketHandlerAdapter 对象
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter () {
        return new WebSocketHandlerAdapter();
    }
}
