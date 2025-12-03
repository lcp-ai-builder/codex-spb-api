package com.lcp.spb.controller;

import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import com.lcp.spb.logic.services.ElasticsearchCryptoTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/es/trades")
public class ElasticsearchCryptoTradeController {

  @Autowired
  private ElasticsearchCryptoTradeService elasticsearchCryptoTradeService;

  @PostMapping
  public Mono<CryptoTradeInfo> saveTrade(@RequestBody CryptoTradeInfo tradeInfo) {
    return elasticsearchCryptoTradeService.save(tradeInfo);
  }

  @GetMapping
  public Flux<CryptoTradeInfo> searchTrades(
      @RequestParam(value = "userId", required = false) String userId,
      @RequestParam(value = "symbol", required = false) CryptoCurrency symbol,
      @RequestParam(value = "side", required = false) TradeSide side,
      @RequestParam(value = "orderType", required = false) OrderType orderType,
      @RequestParam(value = "status", required = false) OrderStatus status,
      @RequestParam(value = "exchange", required = false) String exchange,
      @RequestParam(value = "notesKeyword", required = false) String notesKeyword,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "size", defaultValue = "20") int size) {
    return elasticsearchCryptoTradeService.search(
        userId, symbol, side, orderType, status, exchange, notesKeyword, page, size);
  }
}
