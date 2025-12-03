package com.lcp.spb.logic.services;

import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ElasticsearchCryptoTradeService {

    Mono<CryptoTradeInfo> save(CryptoTradeInfo tradeInfo);

    Flux<CryptoTradeInfo> search(
        String userId,
        CryptoCurrency symbol,
        TradeSide side,
        OrderType orderType,
        OrderStatus status,
        String exchange,
        String notesKeyword,
        int page,
        int size);
}
