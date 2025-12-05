package com.lcp.spb.config;

import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import com.lcp.spb.logic.services.ElasticsearchCryptoTradeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Sinks;

/**
 * 定时推送最近一小时交易汇总（带回退）到 WebSocket。
 */
@Configuration
public class TradeSummaryScheduler {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ElasticsearchCryptoTradeService cryptoTradeService;

    private final Sinks.Many<RecentHourTradeSummary> tradeSummarySink;

    public TradeSummaryScheduler() {
        this.tradeSummarySink = Sinks.many().replay().latest();
    }

    public Sinks.Many<RecentHourTradeSummary> getTradeSummarySink () {
        return tradeSummarySink;
    }

    // 每分钟推送一次
    @Scheduled(cron = "0 * * * * ?")
    public void pushSummary () {
        long start = System.currentTimeMillis();
        cryptoTradeService.recentHourSummary()
                .doOnError(error -> tradeSummarySink.tryEmitNext(
                        new RecentHourTradeSummary(0, null, 0, 0, false)))
                .subscribe(summary -> {
                    tradeSummarySink.tryEmitNext(summary);
                    long cost = System.currentTimeMillis() - start;
                    logger.info("pushSummary executed in {} ms, window {}~{}", cost,
                            summary.getWindowStart(), summary.getWindowEnd());
                }, error -> {
                    long cost = System.currentTimeMillis() - start;
                    logger.warn("pushSummary failed in {} ms: {}", cost, error.getMessage());
                });
    }
}
