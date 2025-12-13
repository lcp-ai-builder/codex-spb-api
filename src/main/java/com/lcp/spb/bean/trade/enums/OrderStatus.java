package com.lcp.spb.bean.trade.enums;

/**
 * 订单状态枚举
 * 
 * <p>表示订单的成交状态。
 * 
 * @author lcp
 */
public enum OrderStatus {
    /** 已完全成交：订单已全部成交 */
    FILLED,
    /** 部分成交：订单只成交了一部分，还有未成交部分 */
    PARTIAL
}
