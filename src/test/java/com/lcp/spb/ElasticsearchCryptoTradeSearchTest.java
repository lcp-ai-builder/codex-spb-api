package com.lcp.spb;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.SearchTradesResponse;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import com.lcp.spb.logic.services.ElasticsearchCryptoTradeService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@SpringBootTest @TestPropertySource(properties = "spring.test.mock.mockito.enabled=false") @TestExecutionListeners(listeners = {
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class
})
class ElasticsearchCryptoTradeSearchTest {

  private final Logger logging = LoggerFactory.getLogger(ElasticsearchCryptoTradeSearchTest.class);

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
    elasticsearchClient.indices().refresh(r -> r.index(INDEX));
  }

  @Test
  void searchTradesByExchangeAndUser () throws Exception {
    SearchTradesResponse found = tradeService.search(
        "user-search-1",
        CryptoCurrency.BTC,
        null,
        null,
        null,
        "binance",
        null,
        1,
        100).block();
    if (found != null && found.getTrades() != null) {
      logging.info("query result count: {}", found.getTrades().size());
    }
  }

  @Test
  void searchTradesWithoutFiltersReturnsAllPaged () throws Exception {
    CryptoTradeInfo tradeA = baseTrade("user-all-1", "binance");
    CryptoTradeInfo tradeB = baseTrade("user-all-2", "okx");
    CryptoTradeInfo tradeC = baseTrade("user-all-3", "binance futures");

    tradeService.save(tradeA).block();
    tradeService.save(tradeB).block();
    tradeService.save(tradeC).block();
    elasticsearchClient.indices().refresh(r -> r.index(INDEX));

    SearchTradesResponse results = tradeService
        .search(null, null, null, null, null, null, null, 1, 100).block();

    assertFalse(results == null || results.getTrades().isEmpty(),
        "Should return trades when no filters are provided");
    assertTrue(results.getTrades().size() >= 3,
        "Should include inserted trades when querying all");
  }

  @Test
  void searchTradesByNotesFuzzy () throws Exception {

    SearchTradesResponse results = tradeService
        .search(null, null, null, null, null, null, "人工智能", 1, 100).block();

    if (results != null && results.getTrades() != null) {
      results.getTrades().forEach(crypto -> logging.info("crypto:{}", crypto.getTradeId()));
    }
  }

  private CryptoTradeInfo baseTrade (String userId, String exchange) {
    CryptoTradeInfo info = new CryptoTradeInfo();
    info.setTradeId(UUID.randomUUID().toString());
    info.setUserId(userId);
    info.setSymbol(CryptoCurrency.BTC);
    info.setSide(TradeSide.BUY);
    info.setPrice(BigDecimal.valueOf(10000));
    info.setQuantity(BigDecimal.valueOf(0.1));
    info.setFee(BigDecimal.valueOf(1));
    info.setFeeAsset("USDT");
    info.setOrderType(OrderType.LIMIT);
    info.setStatus(OrderStatus.FILLED);
    info.setExecutedAt(System.currentTimeMillis());
    info.setFeeRate(BigDecimal.valueOf(0.001));
    info.setRealizedPnl(BigDecimal.ZERO);
    info.setMarginTrade(false);
    info.setLeverage(1);
    info.setSettleAsset("USDT");
    info.setExchange(exchange);
    info.setNotes("搜索测试");
    info.setTotalAmount(BigDecimal.valueOf(1000));
    info.setOrderId("order-" + UUID.randomUUID());
    info.setTransactionHash(UUID.randomUUID().toString().replace("-", ""));
    info.setWalletAddress("0x" + UUID.randomUUID().toString().replace("-", ""));
    info.setTag("test-search");
    info.setCreatedBy("junit");
    info.setCreatedAt(System.currentTimeMillis());
    return info;
  }
}
