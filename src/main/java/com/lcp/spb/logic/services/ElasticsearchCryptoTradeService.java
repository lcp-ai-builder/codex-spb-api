package com.lcp.spb.logic.services;

import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ElasticsearchCryptoTradeService {

    Mono<CryptoTradeInfo> save (CryptoTradeInfo tradeInfo);

    Mono<List<CryptoTradeInfo>> search (
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
