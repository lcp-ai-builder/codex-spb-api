package com.lcp.spb.logic.services.impls;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import com.lcp.spb.bean.trade.SearchTradesResponse;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import com.lcp.spb.logic.services.BaseService;
import com.lcp.spb.logic.services.ElasticsearchCryptoTradeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ElasticsearchCryptoTradeServiceImpl extends BaseService
        implements ElasticsearchCryptoTradeService {

    // 交易索引的增删查及最近一小时汇总服务
    private static final String INDEX = "crypto-trade-info";
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;
    private static final String TOTAL_AMOUNT_AGG = "total_amount";
    private static final int MAX_PAGE_SIZE = 1000;

    // 保存/更新交易文档
    @Override
    public Mono<CryptoTradeInfo> save (CryptoTradeInfo tradeInfo) {
        return fromBlocking( () -> elasticsearchClient.index(builder -> {
            // 写入索引，如果传入 tradeId 则使用作为文档 _id
            builder.index(INDEX).document(tradeInfo);
            if (Objects.nonNull(tradeInfo.getTradeId())) {
                builder.id(tradeInfo.getTradeId());
            }
            return builder;
        })).map(response -> {
            // 将 ES 生成的 id 回填到对象，便于前端使用
            tradeInfo.setTradeId(response.id());
            return tradeInfo;
        });
    }

    // 交易分页检索，支持多条件过滤
    @Override
    public Mono<SearchTradesResponse> search (
            String userId, CryptoCurrency symbol, TradeSide side, OrderType orderType,
            OrderStatus status, String exchange, String notesKeyword, int page, int size) {

        // 防御性分页参数，限制最大 size
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int from = (safePage - 1) * safeSize;
        // 构建 bool 过滤条件
        Query filters = buildFilters(userId, symbol, side, orderType, status, exchange,
                notesKeyword);

        // 先统计总数（受 MAX_PAGE_SIZE 限制）
        Mono<Long> totalMono = fromBlocking(
                () -> elasticsearchClient.count(c -> c.index(INDEX).query(filters)).count())
                        .map(count -> Math.min(count, (long) MAX_PAGE_SIZE));

        Mono<SearchTradesResponse> tradesMono = fromBlocking(
                () -> elasticsearchClient.search(searchRequest -> {
                    // ES from/size 分页查询
                    searchRequest.index(INDEX).from(from).size(safeSize);
                    searchRequest.query(filters);
                    return searchRequest;
                }, CryptoTradeInfo.class))
                        .flatMapMany(response -> Flux.fromIterable(
                                Optional.ofNullable(response.hits())
                                        .map(searchHits -> searchHits.hits())
                                        .orElseGet(java.util.List::of)))
                        .map(this::attachIdSafely)
                        .collectList()
                        .zipWith(totalMono, (trades, total) -> new SearchTradesResponse(trades,
                                total, safePage, safeSize));

        return tradesMono;
    }

    // 最近一小时汇总，若无数据则回退到最近有数据的一小时窗口
    @Override
    public Mono<RecentHourTradeSummary> recentHourSummary () {
        long now = System.currentTimeMillis();
        long windowStart = now - ONE_HOUR_MILLIS;

        return aggregateWindow(windowStart, now, false)
                .flatMap(summary -> {
                    // 当前一小时有数据则直接返回
                    boolean hasData = summary.getCount() > 0 ||
                            (Objects.nonNull(summary.getTotalAmount())
                                    && summary.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
                    if (hasData) {
                        return Mono.just(summary);
                    }
                    // 否则查找最新成交时间，构造回退窗口
                    return findLatestExecutedAt()
                            .flatMap(latest -> {
                                if (Objects.isNull(latest)) {
                                    return Mono.just(summary);
                                }
                                long fallbackEnd = latest;
                                long fallbackStart = Math.max(0, fallbackEnd - ONE_HOUR_MILLIS);
                                return aggregateWindow(fallbackStart, fallbackEnd, true);
                            });
                });
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
            if (Objects.nonNull(symbol)) {
                b.must(term("symbol", symbol.name()));
            }
            if (Objects.nonNull(side)) {
                b.must(term("side", side.name()));
            }
            if (Objects.nonNull(orderType)) {
                b.must(term("orderType", orderType.name()));
            }
            if (Objects.nonNull(status)) {
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

    // 构造 match 查询
    private Query match (String field, String text) {
        return Query.of(q -> q.match(m -> m.field(field).query(text)));
    }

    // 组装命中结果，确保 tradeId 补齐
    private CryptoTradeInfo attachIdSafely (Hit<CryptoTradeInfo> hit) {
        CryptoTradeInfo info = hit.source();
        if (Objects.nonNull(info) && Objects.isNull(info.getTradeId())) {
            info.setTradeId(hit.id());
        }
        return info;
    }

    // 聚合计算指定窗口的笔数和总额，失败时兜底为 0
    private Mono<RecentHourTradeSummary> aggregateWindow (long windowStart, long windowEnd,
            boolean fallback) {
        return fromBlocking( () -> elasticsearchClient.search(searchRequest -> {
            searchRequest.index(INDEX);
            searchRequest.size(0);
            searchRequest.trackTotalHits(track -> track.enabled(true));
            searchRequest.query(queryBuilder -> queryBuilder.range(rangeBuilder -> rangeBuilder
                    .date(dateRange -> dateRange.field("executedAt")
                            .gte(String.valueOf(windowStart))
                            .lte(String.valueOf(windowEnd)))));
            // sum 聚合计算总成交金额
            searchRequest.aggregations(TOTAL_AMOUNT_AGG,
                    aggregationBuilder -> aggregationBuilder.sum(sum -> sum.field("totalAmount")));
            return searchRequest;
        }, CryptoTradeInfo.class))
                .map(response -> {
                    // 安全获取 total hits
                    long totalHits = Optional.ofNullable(response.hits())
                            .map(h -> h.total())
                            .map(t -> t.value())
                            .orElse(0L);

                    // 安全获取 sum 聚合值，缺失时默认为 0
                    BigDecimal totalAmount = Optional.ofNullable(response.aggregations())
                            .map(aggs -> aggs.get(TOTAL_AMOUNT_AGG))
                            .map(a -> a.sum())
                            .map(sum -> Optional.ofNullable(sum.value())
                                    .map(value -> BigDecimal.valueOf(value))
                                    .orElse(BigDecimal.ZERO))
                            .orElse(BigDecimal.ZERO);
                    return new RecentHourTradeSummary(totalHits, totalAmount, windowStart,
                            windowEnd, fallback);
                })
                .onErrorResume(ex -> {
                    // 聚合异常时记录并兜底返回 0
                    logger.warn("Failed to aggregate trades window {}~{}, fallback={}: {}",
                            windowStart,
                            windowEnd, fallback, ex.getMessage());
                    return Mono.just(new RecentHourTradeSummary(0L, BigDecimal.ZERO, windowStart,
                            windowEnd, fallback));
                });
    }

    // 查找最新成交时间，供回退窗口使用
    private Mono<Long> findLatestExecutedAt () {
        return fromBlocking( () -> elasticsearchClient.search(searchRequest -> {
            // 按 executedAt 倒序取一条
            searchRequest.index(INDEX);
            searchRequest.size(1);
            searchRequest.sort(sort -> sort.field(fieldSort -> fieldSort.field("executedAt")
                    .order(SortOrder.Desc)));
            return searchRequest;
        }, CryptoTradeInfo.class))
                .map(response -> Optional.ofNullable(response.hits())
                        .map(hits -> hits.hits())
                        .map(list -> list.stream()
                                .map(Hit::source)
                                .filter(Objects::nonNull)
                                .map(CryptoTradeInfo::getExecutedAt)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null))
                        .orElse(null));
    }
}
