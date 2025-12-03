package com.lcp.spb.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import com.lcp.spb.logic.services.ElasticsearchCryptoTradeService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) @AutoConfigureWebTestClient
class ElasticsearchCryptoTradeControllerTest {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(ElasticsearchCryptoTradeControllerTest.class);

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ElasticsearchCryptoTradeService tradeService;

  @Test
  void searchTradesReturnsExpectedList () {
    List<CryptoTradeInfo> trades = List.of(
        sampleTrade("t-1", "u1"),
        sampleTrade("t-2", "u1"));

    when(tradeService.search(
        eq("u1"),
        eq(CryptoCurrency.BTC),
        eq(TradeSide.BUY),
        eq(OrderType.LIMIT),
        eq(OrderStatus.FILLED),
        eq("binance"),
        eq("人工智能"),
        eq(1),
        eq(5)))
            .thenReturn(Mono.just(trades));

    webTestClient
        .get()
        .uri(
            uriBuilder -> uriBuilder
                .path("/es/trades")
                .queryParam("userId", "u1")
                .queryParam("symbol", CryptoCurrency.BTC)
                .queryParam("side", TradeSide.BUY)
                .queryParam("orderType", OrderType.LIMIT)
                .queryParam("status", OrderStatus.FILLED)
                .queryParam("exchange", "binance")
                .queryParam("notesKeyword", "人工智能")
                .queryParam("page", 1)
                .queryParam("size", 5)
                .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(CryptoTradeInfo.class)
        .hasSize(2)
        .contains(trades.get(0), trades.get(1));

    // Log returned data for visibility
    webTestClient
        .get()
        .uri(
            uriBuilder -> uriBuilder
                .path("/es/trades")
                .queryParam("userId", "u1")
                .queryParam("symbol", CryptoCurrency.BTC)
                .queryParam("side", TradeSide.BUY)
                .queryParam("orderType", OrderType.LIMIT)
                .queryParam("status", OrderStatus.FILLED)
                .queryParam("exchange", "binance")
                .queryParam("notesKeyword", "人工智能")
                .queryParam("page", 1)
                .queryParam("size", 5)
                .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(CryptoTradeInfo.class)
        .value(list -> log.info("searchTrades returned: {}", list));
  }

  @TestConfiguration
  static class MockConfig {

    @Bean
    @Primary
    ElasticsearchCryptoTradeService tradeServiceMock() {
      return Mockito.mock(ElasticsearchCryptoTradeService.class);
    }
  }

  private CryptoTradeInfo sampleTrade (String id, String userId) {
    CryptoTradeInfo info = new CryptoTradeInfo();
    info.setTradeId(id);
    info.setUserId(userId);
    info.setSymbol(CryptoCurrency.BTC);
    info.setSide(TradeSide.BUY);
    info.setPrice(BigDecimal.valueOf(10000));
    info.setQuantity(BigDecimal.valueOf(0.1));
    info.setFee(BigDecimal.ONE);
    info.setFeeAsset("USDT");
    info.setOrderType(OrderType.LIMIT);
    info.setStatus(OrderStatus.FILLED);
    info.setExecutedAt(System.currentTimeMillis());
    info.setFeeRate(BigDecimal.valueOf(0.001));
    info.setRealizedPnl(BigDecimal.ZERO);
    info.setMarginTrade(false);
    info.setLeverage(1);
    info.setSettleAsset("USDT");
    info.setExchange("binance");
    info.setNotes("人工智能策略快速成交");
    info.setTotalAmount(BigDecimal.valueOf(1000));
    info.setOrderId("order-" + id);
    info.setTransactionHash("hash-" + id);
    info.setWalletAddress("0x" + id);
    info.setTag("test");
    info.setCreatedBy("junit");
    info.setCreatedAt(System.currentTimeMillis());
    return info;
  }
}
