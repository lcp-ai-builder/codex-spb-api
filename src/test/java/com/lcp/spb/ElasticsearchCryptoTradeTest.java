package com.lcp.spb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import com.lcp.spb.logic.services.ElasticsearchCryptoTradeService;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@SpringBootTest @TestPropertySource(properties = "spring.test.mock.mockito.enabled=false") @TestExecutionListeners(listeners = {
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class
})
class ElasticsearchCryptoTradeTest {

  private static final String INDEX = "crypto-trade-info";

  @Autowired
  private ElasticsearchClient elasticsearchClient;

  @Autowired
  private ElasticsearchCryptoTradeService tradeService;

  @BeforeEach
  void ensureElasticsearchIsAvailable () throws IOException {
    assumeTrue(elasticsearchClient.ping().value(), "Elasticsearch not reachable");
    assumeTrue(elasticsearchClient.indices().exists(r -> r.index(INDEX)).value(),
        "Index crypto-trade-info is missing");
  }

  @Test
  void createIndexAndSaveRandomTrades () throws Exception {
    // 刷新后再计数，避免旧数据未可见
    elasticsearchClient.indices().refresh(r -> r.index(INDEX));
    long before = elasticsearchClient.count(c -> c.index(INDEX)).count();

    int tradesToInsert = 100;
    for (int i = 0; i < tradesToInsert; i++) {
      tradeService.save(randomTrade()).block();
    }

    // 刷新索引，确保计数包含刚写入的数据
    elasticsearchClient.indices().refresh(r -> r.index(INDEX));
    long after = elasticsearchClient.count(c -> c.index(INDEX)).count();
    assertEquals(tradesToInsert, after - before, "Should persist expected number of trades");
  }

  private CryptoTradeInfo randomTrade () {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    BigDecimal price = BigDecimal.valueOf(random.nextDouble(1000, 50000))
        .setScale(2, RoundingMode.HALF_UP);
    BigDecimal quantity = BigDecimal.valueOf(random.nextDouble(0.01, 2))
        .setScale(6, RoundingMode.HALF_UP);
    BigDecimal feeRate = BigDecimal.valueOf(random.nextDouble(0.0005, 0.005))
        .setScale(6, RoundingMode.HALF_UP);
    BigDecimal fee = price.multiply(quantity).multiply(feeRate).setScale(6, RoundingMode.HALF_UP);
    BigDecimal totalAmount = price.multiply(quantity).setScale(6, RoundingMode.HALF_UP);

    CryptoTradeInfo info = new CryptoTradeInfo();
    info.setTradeId(UUID.randomUUID().toString());
    info.setUserId("user-" + random.nextInt(1, 50));
    info.setSymbol(random.nextBoolean() ? CryptoCurrency.BTC : CryptoCurrency.USDT);
    info.setSide(random.nextBoolean() ? TradeSide.BUY : TradeSide.SELL);
    info.setPrice(price);
    info.setQuantity(quantity);
    info.setFee(fee);
    info.setFeeAsset("USDT");
    info.setOrderType(random.nextBoolean() ? OrderType.LIMIT : OrderType.MARKET);
    info.setStatus(random.nextBoolean() ? OrderStatus.FILLED : OrderStatus.PARTIAL);
    info.setExecutedAt(System.currentTimeMillis());
    info.setFeeRate(feeRate);
    info.setRealizedPnl(
        BigDecimal.valueOf(random.nextDouble(-50, 200)).setScale(2, RoundingMode.HALF_UP));
    info.setMarginTrade(random.nextBoolean());
    info.setLeverage(random.nextInt(1, 20));
    info.setSettleAsset("USDT");
    info.setExchange(random.nextBoolean() ? "binance" : "okx");
    info.setNotes(randomChineseNotes());
    info.setTotalAmount(totalAmount);
    info.setOrderId("order-" + UUID.randomUUID());
    info.setTransactionHash(UUID.randomUUID().toString().replace("-", ""));
    info.setWalletAddress("0x" + UUID.randomUUID().toString().replace("-", ""));
    info.setTag("test");
    info.setCreatedBy("junit");
    info.setCreatedAt(System.currentTimeMillis());
    return info;
  }

  private String randomChineseNotes () {
    String[] phrases = {
        "快速成交", "回调买入", "突破加仓", "止盈减仓", "测试分词效果",
        "低位埋伏", "高位卖出", "震荡区间", "策略交易", "机器人下单",
        "风险可控", "滑点较小", "等待回踩", "趋势向上", "情绪降温",
        "放量突破", "缩量震荡", "资金流入", "套利交易", "双向对冲",
        "网格策略", "定投买入", "短线套利", "合约开多", "现货卖出",
        "关注盘口", "保持仓位", "复盘记录", "行情反转", "指标共振",
        "人工智能选币", "AI风控", "机器学习回测", "量化模型优化", "智能交易信号",
        "宏观经济预期", "金融市场波动", "政策利好", "避险情绪", "经济数据发布",
        "政治局势影响", "监管政策收紧", "美元指数走强", "通胀压力", "利率决议",
        "区块链创新", "数据挖掘", "情绪分析", "大模型研判", "深度学习择时",
        "美联储加息预期", "美联储议息会议", "鲍威尔讲话", "华尔街情绪", "华尔街机构调仓",
        "欧洲央行政策", "欧元区通胀", "英国央行加息", "欧洲银行业压力测试", "德法经济数据",
        "欧洲能源危机", "欧洲股市波动", "欧美利差变化", "美元流动性收缩", "全球风险偏好"
    };
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int first = random.nextInt(phrases.length);
    int second = random.nextInt(phrases.length);
    int third = random.nextInt(phrases.length);
    return phrases[first] + "，" + phrases[second] + "，" + phrases[third];
  }
}
