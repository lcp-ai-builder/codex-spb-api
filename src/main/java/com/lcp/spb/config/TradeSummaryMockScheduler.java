package com.lcp.spb.config;

import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Sinks;

/**
 * 测试用定时任务：推送随机交易汇总数据，便于前端演示。
 * 通过配置 trade.summary.mock.enabled=true 启用，默认关闭。
 */
@Configuration
@ConditionalOnProperty(name = "trade.summary.mock.enabled", havingValue = "true")
public class TradeSummaryMockScheduler {

    private static final Logger log = LoggerFactory.getLogger(TradeSummaryMockScheduler.class);

    @Autowired
    private Sinks.Many<RecentHourTradeSummary> tradeSummarySink;

    @Scheduled(cron = "0 * * * * ?")
    public void pushMockSummary () {
        long start = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        long windowStart = now - 60 * 60 * 1000L;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long count = random.nextLong(0, 200);
        BigDecimal amount = BigDecimal.valueOf(random.nextDouble(0, 100_000));
        RecentHourTradeSummary summary =
                new RecentHourTradeSummary(count, amount, windowStart, now, false);
        tradeSummarySink.tryEmitNext(summary);
        long cost = System.currentTimeMillis() - start;
        log.info("Mock push summary: count={}, amount={}, window {}~{}, cost {} ms", count,
                summary.getTotalAmount(), windowStart, now, cost);
    }
}
