package com.lcp.spb.logic.services.impls;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.math.BigDecimal;
import java.util.List;
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

/**
 * 加密货币交易服务实现类
 * 
 * <p>实现加密货币交易数据的核心业务逻辑，包括：
 * <ul>
 *   <li>交易数据的保存和更新</li>
 *   <li>多条件组合查询和分页</li>
 *   <li>最近一小时交易汇总统计（支持回退机制）</li>
 * </ul>
 * 
 * <p>所有操作均基于 Elasticsearch 的 "crypto-trade-info" 索引。
 * 
 * @author lcp
 */
@Service
public class ElasticsearchCryptoTradeServiceImpl extends BaseService
        implements ElasticsearchCryptoTradeService {

    /** Elasticsearch 索引名称，用于存储加密货币交易数据 */
    private static final String INDEX = "crypto-trade-info";
    /** 一小时的毫秒数，用于时间窗口计算 */
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;
    /** 聚合查询中总金额聚合的键名 */
    private static final String TOTAL_AMOUNT_AGG = "total_amount";
    /** 分页查询的最大每页记录数，防止查询过大导致性能问题 */
    private static final int MAX_PAGE_SIZE = 1000;

    /**
     * 保存或更新交易文档
     * 
     * <p>将交易信息保存到 Elasticsearch 索引中。如果交易信息中包含 tradeId，
     * 则使用该 ID 作为文档 ID（可用于更新操作）；否则由 Elasticsearch 自动生成。
     * 
     * <p>保存成功后，会将 Elasticsearch 返回的文档 ID 回填到交易对象中，
     * 便于后续使用。
     * 
     * @param tradeInfo 交易信息对象，包含交易的所有详细信息
     * @return Mono 包装的交易信息对象，包含保存后的 tradeId
     */
    @Override
    public Mono<CryptoTradeInfo> save (CryptoTradeInfo tradeInfo) {
        return saveDocument(INDEX, tradeInfo,
                CryptoTradeInfo::getTradeId,
                CryptoTradeInfo::setTradeId);
    }

    /**
     * 交易分页检索，支持多条件过滤
     * 
     * <p>根据多个可选条件组合查询交易数据，支持分页返回。
     * 查询过程包括：
     * <ol>
     *   <li>参数校验和标准化：确保分页参数有效，限制最大每页记录数</li>
     *   <li>构建查询条件：根据传入的参数构建 Elasticsearch 查询</li>
     *   <li>统计总数：先查询符合条件的总记录数</li>
     *   <li>分页查询：根据分页参数查询具体记录</li>
     *   <li>结果组装：将查询结果和分页信息组装成响应对象</li>
     * </ol>
     * 
     * <p>查询条件说明：
     * <ul>
     *   <li>精确匹配：userId、symbol、side、orderType、status、exchange</li>
     *   <li>模糊匹配：notesKeyword（对 notes 字段进行全文搜索）</li>
     * </ul>
     * 
     * @param userId 用户ID，可选
     * @param symbol 交易币种，可选
     * @param side 交易方向，可选
     * @param orderType 订单类型，可选
     * @param status 订单状态，可选
     * @param exchange 交易所名称，可选
     * @param notesKeyword 备注关键词，可选，支持模糊匹配
     * @param page 页码，从1开始，如果小于1则自动设置为1
     * @param size 每页记录数，如果小于1则自动设置为1，最大不超过 MAX_PAGE_SIZE
     * @return Mono 包装的查询响应对象，包含交易列表、总数和分页信息
     */
    @Override
    public Mono<SearchTradesResponse> search (
            String userId, CryptoCurrency symbol, TradeSide side, OrderType orderType,
            OrderStatus status, String exchange, String notesKeyword, int page, int size) {

        // 标准化分页参数
        PageParams pageParams = normalizePageParams(page, size);
        // 构建查询条件
        Query filters = buildFilters(userId, symbol, side, orderType, status, exchange,
                notesKeyword);

        // 并行执行总数统计和分页查询
        Mono<Long> totalMono = countTotal(filters);
        Mono<List<CryptoTradeInfo>> tradesMono = searchTrades(filters, pageParams);

        // 合并结果
        return Mono.zip(tradesMono, totalMono)
                .map(tuple -> new SearchTradesResponse(
                        tuple.getT1(),
                        tuple.getT2(),
                        pageParams.page,
                        pageParams.size));
    }

    /**
     * 标准化分页参数
     * 
     * @param page 页码
     * @param size 每页记录数
     * @return 标准化后的分页参数
     */
    private PageParams normalizePageParams (int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int from = (safePage - 1) * safeSize;
        return new PageParams(safePage, safeSize, from);
    }

    /**
     * 分页参数封装类
     */
    private static class PageParams {
        final int page;
        final int size;
        final int from;

        PageParams(int page, int size, int from) {
            this.page = page;
            this.size = size;
            this.from = from;
        }
    }

    /**
     * 统计符合条件的总记录数
     * 
     * @param filters 查询条件
     * @return Mono 包装的总记录数，受 MAX_PAGE_SIZE 限制
     */
    private Mono<Long> countTotal (Query filters) {
        return fromBlocking(
                () -> elasticsearchClient.count(c -> c.index(INDEX).query(filters)).count())
                .map(count -> Math.min(count, (long) MAX_PAGE_SIZE));
    }

    /**
     * 分页查询交易数据
     * 
     * @param filters 查询条件
     * @param pageParams 分页参数
     * @return Mono 包装的交易列表
     */
    private Mono<List<CryptoTradeInfo>> searchTrades (Query filters, PageParams pageParams) {
        return fromBlocking(
                () -> elasticsearchClient.search(searchRequest -> {
                    searchRequest.index(INDEX)
                            .from(pageParams.from)
                            .size(pageParams.size)
                            .query(filters);
                    return searchRequest;
                }, CryptoTradeInfo.class))
                .flatMapMany(this::extractHits)
                .map(this::attachIdSafely)
                .collectList();
    }


    /**
     * 获取最近一小时的交易汇总
     * 
     * <p>统计最近一小时内的交易数据，包括交易笔数和总金额。
     * 
     * <p>回退机制：
     * 如果最近一小时没有交易数据，系统会自动查找最近有数据的一小时窗口进行统计。
     * 这样可以确保即使当前没有交易，也能展示最近的有效数据，提升用户体验。
     * 
     * <p>处理流程：
     * <ol>
     *   <li>计算最近一小时的时间窗口（当前时间往前推一小时）</li>
     *   <li>查询该时间窗口内的交易汇总数据</li>
     *   <li>如果有数据，直接返回</li>
     *   <li>如果没有数据，查找最近一笔交易的成交时间</li>
     *   <li>以该时间为结束点，往前推一小时作为回退窗口</li>
     *   <li>查询回退窗口内的交易汇总数据并返回</li>
     * </ol>
     * 
     * @return Mono 包装的最近一小时交易汇总对象，包含交易笔数、总金额、时间窗口和回退标志
     */
    @Override
    public Mono<RecentHourTradeSummary> recentHourSummary () {
        long now = System.currentTimeMillis();
        long windowStart = now - ONE_HOUR_MILLIS;

        return aggregateWindow(windowStart, now, false)
                .flatMap(summary -> {
                    if (hasData(summary)) {
                        // 有数据则直接返回
                        return Mono.just(summary);
                    }
                    // 没有数据则查找最新成交时间，构造回退窗口
                    return findLatestExecutedAt()
                            .flatMap(latest -> {
                                if (Objects.isNull(latest)) {
                                    // 如果没有任何交易记录，返回空汇总
                                    return Mono.just(summary);
                                }
                                // 以最新交易时间为结束点，往前推一小时作为回退窗口
                                long fallbackStart = Math.max(0, latest - ONE_HOUR_MILLIS);
                                return aggregateWindow(fallbackStart, latest, true);
                            });
                });
    }

    /**
     * 构建 Elasticsearch 查询条件
     * 
     * <p>根据传入的多个可选参数构建 Elasticsearch bool 查询。
     * 所有非空参数都会作为 must 条件添加到查询中，实现 AND 逻辑。
     * 
     * <p>查询类型：
     * <ul>
     *   <li>精确匹配（term）：userId、symbol、side、orderType、status、exchange</li>
     *   <li>模糊匹配（match）：notesKeyword（对 notes 字段进行全文搜索）</li>
     * </ul>
     * 
     * @param userId 用户ID，可选
     * @param symbol 交易币种，可选
     * @param side 交易方向，可选
     * @param orderType 订单类型，可选
     * @param status 订单状态，可选
     * @param exchange 交易所名称，可选
     * @param notesKeyword 备注关键词，可选
     * @return Elasticsearch Query 对象，包含所有非空条件的 bool 查询
     */
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

    /**
     * 构建精确匹配查询（term query）
     * 
     * <p>用于对字段进行精确匹配，适用于不分词的字段（如枚举值、ID等）。
     * 
     * @param field 字段名
     * @param value 匹配值
     * @return Elasticsearch Query 对象
     */
    private Query term (String field, String value) {
        return Query.of(q -> q.term(t -> t.field(field).value(v -> v.stringValue(value))));
    }

    /**
     * 构建全文搜索查询（match query）
     * 
     * <p>用于对字段进行全文搜索，支持分词和模糊匹配。
     * 适用于文本字段的模糊搜索场景。
     * 
     * @param field 字段名
     * @param text 搜索文本
     * @return Elasticsearch Query 对象
     */
    private Query match (String field, String text) {
        return Query.of(q -> q.match(m -> m.field(field).query(text)));
    }

    /**
     * 判断汇总数据是否有效（有数据）
     * 
     * <p>判断汇总数据是否包含有效的交易数据：
     * 交易笔数大于0或总金额大于0。
     * 
     * @param summary 交易汇总对象
     * @return true 表示有数据，false 表示无数据
     */
    private boolean hasData (RecentHourTradeSummary summary) {
        return summary.getCount() > 0 ||
                (Objects.nonNull(summary.getTotalAmount())
                        && summary.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * 安全地附加文档ID到交易信息对象
     * 
     * <p>从 Elasticsearch 查询结果中提取文档ID，如果交易对象中没有 tradeId，
     * 则将文档ID设置到对象中，确保返回的对象包含完整的标识信息。
     * 
     * @param hit Elasticsearch 查询命中结果
     * @return 包含 tradeId 的交易信息对象
     */
    private CryptoTradeInfo attachIdSafely (Hit<CryptoTradeInfo> hit) {
        return attachIdFromHit(hit, CryptoTradeInfo::getTradeId, CryptoTradeInfo::setTradeId);
    }

    /**
     * 聚合计算指定时间窗口内的交易笔数和总金额
     * 
     * <p>使用 Elasticsearch 的聚合功能统计指定时间范围内的交易数据：
     * <ul>
     *   <li>使用 count 聚合统计交易笔数</li>
     *   <li>使用 sum 聚合计算总金额</li>
     * </ul>
     * 
     * <p>异常处理：
     * 如果聚合查询失败，会记录警告日志并返回空汇总（笔数为0，总金额为0），
     * 确保系统不会因为查询异常而崩溃。
     * 
     * @param windowStart 时间窗口开始时间（毫秒时间戳）
     * @param windowEnd 时间窗口结束时间（毫秒时间戳）
     * @param fallback 是否为回退窗口，用于标识汇总数据的来源
     * @return Mono 包装的交易汇总对象，包含交易笔数、总金额、时间窗口和回退标志
     */
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
                    long totalHits = extractTotalHits(response);
                    BigDecimal totalAmount = extractTotalAmount(response);
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

    /**
     * 从聚合响应中提取总命中数
     * 
     * @param response 查询响应
     * @return 总命中数，如果无法获取则返回 0
     */
    private long extractTotalHits (
            co.elastic.clients.elasticsearch.core.SearchResponse<CryptoTradeInfo> response) {
        return Optional.ofNullable(response.hits())
                .map(h -> h.total())
                .map(t -> t.value())
                .orElse(0L);
    }

    /**
     * 从聚合响应中提取总金额
     * 
     * @param response 查询响应
     * @return 总金额，如果无法获取则返回 0
     */
    private BigDecimal extractTotalAmount (
            co.elastic.clients.elasticsearch.core.SearchResponse<CryptoTradeInfo> response) {
        return Optional.ofNullable(response.aggregations())
                .map(aggs -> aggs.get(TOTAL_AMOUNT_AGG))
                .map(a -> a.sum())
                .map(sum -> Optional.ofNullable(sum.value())
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ZERO))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 查找最新一笔交易的成交时间
     * 
     * <p>用于回退机制：当最近一小时没有交易数据时，需要找到最近一笔交易的成交时间，
     * 以便构造回退窗口进行统计。
     * 
     * <p>查询方式：
     * <ul>
     *   <li>按 executedAt 字段倒序排序</li>
     *   <li>只查询一条记录（size=1）</li>
     *   <li>提取该记录的 executedAt 字段值</li>
     * </ul>
     * 
     * @return Mono 包装的最新成交时间（毫秒时间戳），如果没有交易记录则返回 null
     */
    private Mono<Long> findLatestExecutedAt () {
        return fromBlocking( () -> elasticsearchClient.search(searchRequest -> {
            searchRequest.index(INDEX)
                    .size(1)
                    .sort(sort -> sort.field(fieldSort -> fieldSort.field("executedAt")
                            .order(SortOrder.Desc)));
            return searchRequest;
        }, CryptoTradeInfo.class))
                .flatMapMany(this::extractHits)
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(CryptoTradeInfo::getExecutedAt)
                .filter(Objects::nonNull)
                .next() // 获取第一个元素，如果没有则返回空 Mono
                .cast(Long.class);
    }
}
