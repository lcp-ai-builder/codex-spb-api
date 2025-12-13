package com.lcp.spb.config;

import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * 交易汇总数据流配置类
 * 
 * <p>提供交易汇总推送使用的共享 Sink（数据流源）。
 * 
 * <p>功能说明：
 * <ul>
 *   <li>使用 Reactor 的 Sinks 创建响应式数据流源</li>
 *   <li>配置为 replay().latest()：只保留最新的数据，新订阅者会立即收到最新数据</li>
 *   <li>用于定时任务推送交易汇总数据到 WebSocket 客户端</li>
 * </ul>
 * 
 * <p>使用场景：
 * <ul>
 *   <li>定时任务将交易汇总数据推送到 Sink</li>
 *   <li>WebSocket 处理器从 Sink 订阅数据并广播给客户端</li>
 * </ul>
 * 
 * @author lcp
 */
@Configuration
public class TradeSummarySinkConfig {

    /**
     * 创建交易汇总数据流 Sink
     * 
     * <p>创建一个支持多播的 Sink，用于推送交易汇总数据。
     * 使用 replay().latest() 策略，确保新订阅者能立即收到最新数据。
     * 
     * @return Sinks.Many 对象，用于推送和订阅交易汇总数据
     */
    @Bean
    public Sinks.Many<RecentHourTradeSummary> tradeSummarySink () {
        return Sinks.many().replay().latest();
    }
}
