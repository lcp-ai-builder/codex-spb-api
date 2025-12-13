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
 * 交易汇总模拟数据定时推送调度器
 * 
 * <p>用于测试和演示的定时任务，推送随机生成的交易汇总数据到 WebSocket。
 * 
 * <p>功能特性：
 * <ul>
 *   <li>定时执行：每分钟执行一次（cron: "0 * * * * ?"）</li>
 *   <li>随机数据生成：生成随机的交易笔数和总金额</li>
 *   <li>数据推送：将模拟数据推送到 Sink，由 WebSocket 处理器广播给客户端</li>
 *   <li>性能监控：记录每次执行的耗时</li>
 * </ul>
 * 
 * <p>使用场景：
 * <ul>
 *   <li>前端开发和演示：无需真实数据即可测试前端功能</li>
 *   <li>系统测试：验证 WebSocket 推送功能是否正常</li>
 *   <li>性能测试：测试系统在高频数据推送下的表现</li>
 * </ul>
 * 
 * <p>启用条件：
 * 只有当配置项 trade.summary.mock.enabled 为 true 时才会启用。
 * 默认情况下该配置为 false，使用 {@link TradeSummaryScheduler} 进行真实数据推送。
 * 
 * <p>数据范围：
 * <ul>
 *   <li>交易笔数：0-200（随机）</li>
 *   <li>总金额：0-100,000（随机）</li>
 *   <li>时间窗口：当前时间往前推一小时</li>
 * </ul>
 * 
 * @author lcp
 */
@Configuration
@ConditionalOnProperty(name = "trade.summary.mock.enabled", havingValue = "true")
public class TradeSummaryMockScheduler {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(TradeSummaryMockScheduler.class);

    /** 交易汇总数据流源，用于推送数据到 WebSocket */
    @Autowired
    private Sinks.Many<RecentHourTradeSummary> tradeSummarySink;

    /**
     * 定时推送模拟交易汇总数据
     * 
     * <p>每分钟执行一次，生成随机的交易汇总数据并推送到 WebSocket。
     * 
     * <p>执行流程：
     * <ol>
     *   <li>记录开始时间，用于性能监控</li>
     *   <li>计算时间窗口（当前时间往前推一小时）</li>
     *   <li>生成随机的交易笔数和总金额</li>
     *   <li>创建交易汇总对象</li>
     *   <li>将数据推送到 Sink</li>
     *   <li>记录执行耗时和推送的数据</li>
     * </ol>
     * 
     * <p>定时表达式：0 * * * * ?（每分钟的第0秒执行）
     */
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
