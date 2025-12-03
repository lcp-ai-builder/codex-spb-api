package com.lcp.spb.logic.services.impls;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.SearchTradesResponse;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import com.lcp.spb.logic.services.BaseService;
import com.lcp.spb.logic.services.ElasticsearchCryptoTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ElasticsearchCryptoTradeServiceImpl extends BaseService
        implements ElasticsearchCryptoTradeService {

    private static final String INDEX = "crypto-trade-info";
    private static final int MAX_PAGE_SIZE = 1000;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public Mono<CryptoTradeInfo> save (CryptoTradeInfo tradeInfo) {
        return fromBlocking( () -> elasticsearchClient.index(builder -> {
            builder.index(INDEX).document(tradeInfo);
            if (tradeInfo.getTradeId() != null) {
                builder.id(tradeInfo.getTradeId());
            }
            return builder;
        })).map(response -> {
            tradeInfo.setTradeId(response.id());
            return tradeInfo;
        });
    }

    @Override
    public Mono<SearchTradesResponse> search (
            String userId, CryptoCurrency symbol, TradeSide side, OrderType orderType,
            OrderStatus status, String exchange, String notesKeyword, int page, int size) {

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int from = (safePage - 1) * safeSize;
        Query filters = buildFilters(userId, symbol, side, orderType, status, exchange, notesKeyword);

        Mono<Long> totalMono = fromBlocking(() ->
                elasticsearchClient.count(c -> c.index(INDEX).query(filters)).count())
            .map(count -> Math.min(count, (long) MAX_PAGE_SIZE));

        Mono<SearchTradesResponse> tradesMono = fromBlocking(() ->
            elasticsearchClient.search(s -> {
                s.index(INDEX).from(from).size(safeSize);
                s.query(filters);
                return s;
            }, CryptoTradeInfo.class))
            .flatMapMany(response -> Flux.fromIterable(response.hits().hits()))
            .map(this::attachIdSafely)
            .collectList()
            .zipWith(totalMono, (trades, total) ->
                new SearchTradesResponse(trades, total, safePage, safeSize));

        return tradesMono;
    }

    private Query buildFilters (
            String userId,
            CryptoCurrency symbol,
            TradeSide side,
            OrderType orderType,
            OrderStatus status,
            String exchange,
            String notesKeyword) {
        return Query.of(q -> q.bool(b -> {
            if (StringUtils.hasText(userId)) {
                b.must(term("userId", userId));
            }
            if (symbol != null) {
                b.must(term("symbol", symbol.name()));
            }
            if (side != null) {
                b.must(term("side", side.name()));
            }
            if (orderType != null) {
                b.must(term("orderType", orderType.name()));
            }
            if (status != null) {
                b.must(term("status", status.name()));
            }
            if (StringUtils.hasText(exchange)) {
                b.must(term("exchange", exchange));
            }
            if (StringUtils.hasText(notesKeyword)) {
                b.must(match("notes", notesKeyword));
            }
            return b;
        }));
    }

    private Query term (String field, String value) {
        return Query.of(q -> q.term(t -> t.field(field).value(v -> v.stringValue(value))));
    }

    private Query match (String field, String text) {
        return Query.of(q -> q.match(m -> m.field(field).query(text)));
    }

    private CryptoTradeInfo attachIdSafely (Hit<CryptoTradeInfo> hit) {
        CryptoTradeInfo info = hit.source();
        if (info != null && info.getTradeId() == null) {
            info.setTradeId(hit.id());
        }
        return info;
    }
}
