package com.lcp.spb.logic.services;

import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import com.lcp.spb.bean.trade.SearchTradesResponse;
import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;
import reactor.core.publisher.Mono;

/**
 * 加密货币交易服务接口
 * 
 * <p>定义加密货币交易数据的核心业务操作，包括：
 * <ul>
 *   <li>交易数据保存：将交易信息持久化到 Elasticsearch</li>
 *   <li>交易数据查询：支持多条件组合查询和分页</li>
 *   <li>交易汇总统计：计算最近一小时的交易汇总信息</li>
 * </ul>
 * 
 * <p>所有方法均返回响应式类型（Mono），支持非阻塞异步处理。
 * 
 * @author lcp
 */
public interface ElasticsearchCryptoTradeService {

    /**
     * 保存交易信息
     * 
     * <p>将交易信息保存到 Elasticsearch 的 "crypto-trade-info" 索引中。
     * 如果交易信息中包含 tradeId，则使用该 ID 作为文档 ID。
     * 
     * @param tradeInfo 交易信息对象，包含交易的所有详细信息
     * @return Mono 包装的交易信息对象，保存成功后会包含 tradeId
     */
    Mono<CryptoTradeInfo> save (CryptoTradeInfo tradeInfo);

    /**
     * 分页查询交易数据
     * 
     * <p>支持多条件组合查询，所有查询条件都是可选的。
     * 查询结果按分页返回，包含交易列表、总数和分页信息。
     * 
     * @param userId 用户ID，可选，精确匹配
     * @param symbol 交易币种，可选，精确匹配
     * @param side 交易方向（买入/卖出），可选，精确匹配
     * @param orderType 订单类型（限价/市价），可选，精确匹配
     * @param status 订单状态，可选，精确匹配
     * @param exchange 交易所名称，可选，精确匹配
     * @param notesKeyword 备注关键词，可选，模糊匹配
     * @param page 页码，从1开始
     * @param size 每页记录数，最大不超过1000
     * @return Mono 包装的查询响应对象，包含交易列表、总数和分页信息
     */
    Mono<SearchTradesResponse> search (
            String userId,
            CryptoCurrency symbol,
            TradeSide side,
            OrderType orderType,
            OrderStatus status,
            String exchange,
            String notesKeyword,
            int page,
            int size);

    /**
     * 获取最近一小时的交易汇总
     * 
     * <p>统计最近一小时内的交易数据，包括交易笔数和总金额。
     * 如果最近一小时没有数据，会自动回退到最近有数据的一小时窗口。
     * 
     * @return Mono 包装的最近一小时交易汇总对象
     */
    Mono<RecentHourTradeSummary> recentHourSummary ();
}
