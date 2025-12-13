package com.lcp.spb.controller;

import com.lcp.spb.bean.trade.CryptoTradeInfo;
import com.lcp.spb.bean.trade.RecentHourTradeSummary;
import com.lcp.spb.bean.trade.SearchTradesResponse;
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
import reactor.core.publisher.Mono;

/**
 * 加密货币交易数据控制器
 * 
 * <p>提供加密货币交易数据的 RESTful API 接口，包括：
 * <ul>
 *   <li>交易数据保存：支持新增和更新交易记录</li>
 *   <li>交易数据查询：支持多条件组合查询和分页</li>
 *   <li>交易汇总统计：获取最近一小时的交易汇总信息</li>
 * </ul>
 * 
 * <p>所有接口均基于响应式编程模型（Reactor），返回 Mono 或 Flux 类型，
 * 支持非阻塞异步处理，提高系统吞吐量。
 * 
 * <p>API 路径前缀：/es/trades
 * 
 * @author lcp
 */
@RestController @RequestMapping("/es/trades")
public class ElasticsearchCryptoTradeController extends AbstractController {

  /** 加密货币交易服务，负责业务逻辑处理 */
  @Autowired
  private ElasticsearchCryptoTradeService elasticsearchCryptoTradeService;

  /**
   * 保存交易数据
   * 
   * <p>将交易信息保存到 Elasticsearch 索引中。如果交易信息中包含 tradeId，
   * 则使用该 ID 作为文档 ID；否则由 Elasticsearch 自动生成。
   * 
   * <p>请求方式：POST /es/trades
   * 
   * @param tradeInfo 交易信息对象，包含交易的所有详细信息
   * @return Mono 包装的交易信息对象，如果保存成功会包含生成的 tradeId
   */
  @PostMapping
  public Mono<CryptoTradeInfo> saveTrade (@RequestBody CryptoTradeInfo tradeInfo) {
    return elasticsearchCryptoTradeService.save(tradeInfo);
  }

  /**
   * 分页查询交易数据
   * 
   * <p>支持多条件组合查询，所有查询条件都是可选的，可以任意组合使用。
   * 查询结果按分页返回，默认每页20条记录。
   * 
   * <p>支持的查询条件：
   * <ul>
   *   <li>userId：用户ID，精确匹配</li>
   *   <li>symbol：交易币种（BTC、USDT等），精确匹配</li>
   *   <li>side：交易方向（买入/卖出），精确匹配</li>
   *   <li>orderType：订单类型（限价/市价），精确匹配</li>
   *   <li>status：订单状态（已成交/部分成交），精确匹配</li>
   *   <li>exchange：交易所名称，精确匹配</li>
   *   <li>notesKeyword：备注关键词，模糊匹配</li>
   *   <li>page：页码，从1开始，默认为1</li>
   *   <li>size：每页记录数，默认为20，最大不超过1000</li>
   * </ul>
   * 
   * <p>请求方式：GET /es/trades
   * 
   * <p>示例请求：
   * <pre>
   * GET /es/trades?userId=123&symbol=BTC&side=BUY&page=1&size=20
   * </pre>
   * 
   * @param userId 用户ID，可选
   * @param symbol 交易币种枚举，可选
   * @param side 交易方向枚举，可选
   * @param orderType 订单类型枚举，可选
   * @param status 订单状态枚举，可选
   * @param exchange 交易所名称，可选
   * @param notesKeyword 备注关键词，可选，支持模糊匹配
   * @param page 页码，从1开始，默认为1
   * @param size 每页记录数，默认为20
   * @return Mono 包装的查询响应对象，包含交易列表、总数和分页信息
   */
  @GetMapping
  public Mono<SearchTradesResponse> searchTrades (
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

  /**
   * 获取最近一小时的交易汇总
   * 
   * <p>统计最近一小时内的交易数据，包括：
   * <ul>
   *   <li>交易笔数（count）</li>
   *   <li>交易总金额（totalAmount）</li>
   *   <li>时间窗口（windowStart 和 windowEnd）</li>
   *   <li>是否使用了回退窗口（fallback）</li>
   * </ul>
   * 
   * <p>回退机制：
   * 如果最近一小时没有交易数据，系统会自动查找最近有数据的一小时窗口进行统计，
   * 此时 fallback 标志为 true。
   * 
   * <p>请求方式：GET /es/trades/summary/recent-hour
   * 
   * @return Mono 包装的最近一小时交易汇总对象
   */
  @GetMapping("/summary/recent-hour")
  public Mono<RecentHourTradeSummary> recentHourSummary () {
    return elasticsearchCryptoTradeService.recentHourSummary();
  }
}
