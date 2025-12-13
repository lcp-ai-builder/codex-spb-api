package com.lcp.spb.config;

import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import com.lcp.spb.logic.services.ElasticsearchCryptoTradeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Sinks;

/**
 * 交易汇总定时推送调度器
 * 
 * <p>定时从 Elasticsearch 查询最近一小时的交易汇总数据，并通过 WebSocket 推送给客户端。
 * 
 * <p>功能特性：
 * <ul>
 *   <li>定时执行：每分钟执行一次（cron: "0 * * * * ?"）</li>
 *   <li>数据查询：调用服务层获取最近一小时交易汇总（支持回退机制）</li>
 *   <li>数据推送：将汇总数据推送到 Sink，由 WebSocket 处理器广播给客户端</li>
 *   <li>异常处理：查询失败时推送空汇总数据，确保系统稳定性</li>
 *   <li>性能监控：记录每次执行的耗时</li>
 * </ul>
 * 
 * <p>启用条件：
 * 只有当配置项 trade.summary.mock.enabled 为 false 或未配置时才会启用。
 * 如果该配置为 true，则使用 {@link TradeSummaryMockScheduler} 进行模拟数据推送。
 * 
 * @author lcp
 */
@Configuration
@ConditionalOnProperty(name = "trade.summary.mock.enabled", havingValue = "false", matchIfMissing = true)
public class TradeSummaryScheduler {

    /** 日志记录器 */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /** 加密货币交易服务，用于查询交易汇总数据 */
    @Autowired
    private ElasticsearchCryptoTradeService cryptoTradeService;

    /** 交易汇总数据流源，用于推送数据到 WebSocket */
    @Autowired
    private Sinks.Many<RecentHourTradeSummary> tradeSummarySink;

    /**
     * 获取交易汇总数据流源
     * 
     * @return Sinks.Many 对象
     */
    public Sinks.Many<RecentHourTradeSummary> getTradeSummarySink () {
        return tradeSummarySink;
    }

    /**
     * 定时推送交易汇总数据
     * 
     * <p>每分钟执行一次，查询最近一小时的交易汇总数据并推送到 WebSocket。
     * 
     * <p>执行流程：
     * <ol>
     *   <li>记录开始时间，用于性能监控</li>
     *   <li>调用服务层查询最近一小时交易汇总</li>
     *   <li>查询成功：将数据推送到 Sink，记录执行耗时</li>
     *   <li>查询失败：推送空汇总数据，记录错误日志和耗时</li>
     * </ol>
     * 
     * <p>定时表达式：0 * * * * ?（每分钟的第0秒执行）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void pushSummary () {
        long start = System.currentTimeMillis();
        cryptoTradeService.recentHourSummary()
                .doOnNext(summary -> {
                    tradeSummarySink.tryEmitNext(summary);
                    long cost = System.currentTimeMillis() - start;
                    logger.info("pushSummary executed in {} ms, window {}~{}", cost,
                            summary.getWindowStart(), summary.getWindowEnd());
                })
                .doOnError(error -> {
                    long cost = System.currentTimeMillis() - start;
                    logger.warn("pushSummary failed in {} ms: {}", cost, error.getMessage());
                    // 推送空汇总数据，确保系统稳定性
                    tradeSummarySink.tryEmitNext(
                            new RecentHourTradeSummary(0, null, 0, 0, false));
                })
                .subscribe(); // 触发执行
    }
}
