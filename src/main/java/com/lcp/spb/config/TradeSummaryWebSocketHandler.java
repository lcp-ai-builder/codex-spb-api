package com.lcp.spb.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WebFlux WebSocket 处理器：向连接的客户端广播最近一小时交易汇总。
 */
@Component
public class TradeSummaryWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private TradeSummaryScheduler tradeSummaryScheduler;

    @Override
    public Mono<Void> handle (WebSocketSession session) {
        Flux<String> payloadFlux = tradeSummaryScheduler.getTradeSummarySink().asFlux()
                .map(this::toJsonSafely)
                .onErrorResume(error -> Flux.empty());
        return session.send(payloadFlux.map(session::textMessage))
                .and(session.receive().then());
    }

    private String toJsonSafely (RecentHourTradeSummary summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException e) {
            return "{\"count\":0,\"totalAmount\":0,\"windowStart\":0,\"windowEnd\":0,\"fallback\":false}";
        }
    }
}
