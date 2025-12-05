package com.lcp.spb.config;

import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * 提供交易汇总推送使用的共享 Sink。
 */
@Configuration
public class TradeSummarySinkConfig {

    @Bean
    public Sinks.Many<RecentHourTradeSummary> tradeSummarySink () {
        return Sinks.many().replay().latest();
    }
}
