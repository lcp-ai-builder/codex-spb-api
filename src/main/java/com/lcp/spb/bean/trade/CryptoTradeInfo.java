package com.lcp.spb.bean.trade;

import java.math.BigDecimal;

import com.lcp.spb.bean.trade.enums.CryptoCurrency;
import com.lcp.spb.bean.trade.enums.OrderStatus;
import com.lcp.spb.bean.trade.enums.OrderType;
import com.lcp.spb.bean.trade.enums.TradeSide;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class CryptoTradeInfo {

    /** 常见的虚拟币交易记录，包含价格、数量、费用和时间等核心信息 */
    private String tradeId; // 交易唯一标识
    private String userId; // 交易所属用户ID
    private CryptoCurrency symbol; // 交易币种
    private TradeSide side; // 买入或卖出方向
    private BigDecimal price; // 成交单价
    private BigDecimal quantity; // 成交数量
    private BigDecimal fee; // 手续费金额
    private String feeAsset; // 手续费计价资产
    private OrderType orderType; // 订单类型
    private OrderStatus status; // 订单状态
    private Long executedAt; // 成交时间戳（毫秒）
    private BigDecimal feeRate; // 手续费率
    private BigDecimal realizedPnl; // 已实现盈亏
    private Boolean marginTrade; // 是否为保证金/合约交易
    private Integer leverage; // 杠杆倍数
    private String settleAsset; // 结算资产
    private String exchange; // 交易所名称
    private String notes; // 备注信息
    private BigDecimal totalAmount; // 成交总金额
    private String orderId; // 关联的订单ID
    private String transactionHash; // 区块链交易哈希
    private String walletAddress; // 相关钱包地址
    private String tag; // 交易标签或分类
    private String createdBy; // 记录创建者
    private Long createdAt; // 记录创建时间戳（毫秒）

}
