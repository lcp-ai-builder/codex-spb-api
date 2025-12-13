package com.lcp.spb.bean.trade;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最近一小时交易汇总对象
 * 
 * <p>封装最近一小时（或回退窗口）内的交易统计数据，用于实时展示和 WebSocket 推送。
 * 
 * <p>数据内容：
 * <ul>
 *   <li>交易笔数：统计时间窗口内的交易记录数量</li>
 *   <li>总金额：统计时间窗口内的交易总金额</li>
 *   <li>时间窗口：统计的时间范围（开始和结束时间戳）</li>
 *   <li>回退标志：标识是否使用了回退窗口（当最近一小时没有数据时）</li>
 * </ul>
 * 
 * @author lcp
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class RecentHourTradeSummary {

  /** 交易笔数，统计时间窗口内的交易记录数量 */
  private long count;
  /** 交易总金额，统计时间窗口内的所有交易金额之和 */
  private BigDecimal totalAmount;
  /** 时间窗口开始时间（毫秒时间戳） */
  private long windowStart;
  /** 时间窗口结束时间（毫秒时间戳） */
  private long windowEnd;
  /** 是否为回退窗口，true 表示使用了回退机制（最近一小时没有数据时） */
  private boolean fallback;
}
