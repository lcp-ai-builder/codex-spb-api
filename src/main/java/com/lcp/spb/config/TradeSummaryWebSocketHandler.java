package com.lcp.spb.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * 交易汇总 WebSocket 处理器
 * 
 * <p>处理 WebSocket 连接，向连接的客户端实时广播最近一小时交易汇总数据。
 * 
 * <p>工作流程：
 * <ol>
 *   <li>客户端建立 WebSocket 连接</li>
 *   <li>处理器订阅交易汇总数据流（Sink）</li>
 *   <li>当有新的交易汇总数据时，自动转换为 JSON 并发送给客户端</li>
 *   <li>保持连接直到客户端断开</li>
 * </ol>
 * 
 * <p>特性：
 * <ul>
 *   <li>自动 JSON 序列化：将交易汇总对象转换为 JSON 字符串</li>
 *   <li>异常处理：序列化失败时返回默认的空汇总数据</li>
 *   <li>错误恢复：数据流异常时自动恢复，不影响连接</li>
 * </ul>
 * 
 * @author lcp
 */
@Component
public class TradeSummaryWebSocketHandler implements WebSocketHandler {

    /** JSON 序列化器，用于将对象转换为 JSON 字符串 */
    @Autowired
    private ObjectMapper objectMapper;

    /** 交易汇总数据流源，用于订阅和推送数据 */
    @Autowired
    private Sinks.Many<RecentHourTradeSummary> tradeSummarySink;

    /**
     * 处理 WebSocket 会话
     * 
     * <p>当客户端建立 WebSocket 连接时，该方法会被调用。
     * 处理器会订阅交易汇总数据流，并将数据实时发送给客户端。
     * 
     * <p>处理逻辑：
     * <ul>
     *   <li>从 Sink 创建数据流（Flux）</li>
     *   <li>将每个交易汇总对象转换为 JSON 字符串</li>
     *   <li>通过 WebSocket 发送文本消息给客户端</li>
     *   <li>监听客户端消息（用于保持连接）</li>
     * </ul>
     * 
     * @param session WebSocket 会话对象
     * @return Mono 对象，表示处理完成
     */
    @Override
    public Mono<Void> handle (WebSocketSession session) {
        Flux<String> payloadFlux = tradeSummarySink.asFlux()
                .map(this::toJsonSafely)
                .onErrorResume(error -> Flux.empty());
        return session.send(payloadFlux.map(session::textMessage))
                .and(session.receive().then());
    }

    /**
     * 安全地将交易汇总对象转换为 JSON 字符串
     * 
     * <p>如果序列化失败，返回默认的空汇总数据，确保不会因为序列化错误而中断数据流。
     * 
     * @param summary 交易汇总对象
     * @return JSON 字符串，如果序列化失败则返回默认的空汇总 JSON
     */
    private String toJsonSafely (RecentHourTradeSummary summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException e) {
            return "{\"count\":0,\"totalAmount\":0,\"windowStart\":0,\"windowEnd\":0,\"fallback\":false}";
        }
    }
}
