package com.lcp.spb.bean.trade;

import java.math.BigDecimal;

import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加密货币交易信息实体类
 * 
 * <p>表示一条完整的加密货币交易记录，包含交易的所有核心信息和扩展信息。
 * 该实体用于在 Elasticsearch 中存储和查询交易数据。
 * 
 * <p>核心字段：
 * <ul>
 *   <li>交易标识：tradeId（唯一标识）</li>
 *   <li>用户信息：userId（交易所属用户）</li>
 *   <li>交易标的：symbol（交易币种）</li>
 *   <li>交易方向：side（买入/卖出）</li>
 *   <li>价格和数量：price、quantity（成交单价和数量）</li>
 *   <li>费用信息：fee、feeRate、feeAsset（手续费相关）</li>
 *   <li>订单信息：orderType、status、orderId（订单类型、状态和ID）</li>
 *   <li>时间信息：executedAt（成交时间）、createdAt（创建时间）</li>
 * </ul>
 * 
 * <p>扩展字段：
 * <ul>
 *   <li>盈亏信息：realizedPnl（已实现盈亏）</li>
 *   <li>合约信息：marginTrade、leverage、settleAsset（保证金交易相关）</li>
 *   <li>交易所信息：exchange（交易所名称）</li>
 *   <li>区块链信息：transactionHash、walletAddress（区块链相关）</li>
 *   <li>其他信息：notes、tag、createdBy（备注、标签、创建者）</li>
 * </ul>
 * 
 * @author lcp
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class CryptoTradeInfo {

    /** 交易唯一标识，在 Elasticsearch 中作为文档ID */
    private String tradeId;
    /** 交易所属用户ID */
    private String userId;
    /** 交易币种枚举（如 BTC、USDT） */
    private CryptoCurrency symbol;
    /** 交易方向：买入（BUY）或卖出（SELL） */
    private TradeSide side;
    /** 成交单价 */
    private BigDecimal price;
    /** 成交数量 */
    private BigDecimal quantity;
    /** 手续费金额 */
    private BigDecimal fee;
    /** 手续费计价资产（如 USDT、BTC） */
    private String feeAsset;
    /** 订单类型：限价单（LIMIT）或市价单（MARKET） */
    private OrderType orderType;
    /** 订单状态：已完全成交（FILLED）或部分成交（PARTIAL） */
    private OrderStatus status;
    /** 成交时间戳（毫秒） */
    private Long executedAt;
    /** 手续费率（如 0.001 表示 0.1%） */
    private BigDecimal feeRate;
    /** 已实现盈亏金额 */
    private BigDecimal realizedPnl;
    /** 是否为保证金/合约交易 */
    private Boolean marginTrade;
    /** 杠杆倍数（如 10 表示 10倍杠杆） */
    private Integer leverage;
    /** 结算资产（合约交易的结算币种） */
    private String settleAsset;
    /** 交易所名称（如 Binance、OKX） */
    private String exchange;
    /** 备注信息，支持关键词搜索 */
    private String notes;
    /** 成交总金额（price * quantity） */
    private BigDecimal totalAmount;
    /** 关联的订单ID */
    private String orderId;
    /** 区块链交易哈希（如果是链上交易） */
    private String transactionHash;
    /** 相关钱包地址 */
    private String walletAddress;
    /** 交易标签或分类（用于自定义分类） */
    private String tag;
    /** 记录创建者（用于审计） */
    private String createdBy;
    /** 记录创建时间戳（毫秒） */
    private Long createdAt;

}
