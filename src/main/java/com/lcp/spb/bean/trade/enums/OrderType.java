package com.lcp.spb.bean.trade.enums;

/**
 * 订单类型枚举
 * 
 * <p>表示订单的类型，即限价单或市价单。
 * 
 * @author lcp
 */
public enum OrderType {
    /** 限价单：指定价格，只有当市场价格达到指定价格时才会成交 */
    LIMIT,
    /** 市价单：以当前市场价格立即成交 */
    MARKET
}
