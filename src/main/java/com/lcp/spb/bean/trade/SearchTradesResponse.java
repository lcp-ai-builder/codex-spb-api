package com.lcp.spb.bean.trade;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易查询响应对象
 * 
 * <p>封装分页查询交易数据的结果，包含交易列表、总数和分页信息。
 * 
 * @author lcp
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class SearchTradesResponse {

  /** 交易列表，包含当前页的交易记录 */
  private List<CryptoTradeInfo> trades;
  /** 符合条件的总记录数 */
  private long total;
  /** 当前页码，从1开始 */
  private int page;
  /** 每页记录数 */
  private int size;
}
