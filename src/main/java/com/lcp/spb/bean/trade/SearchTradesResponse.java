package com.lcp.spb.bean.trade;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class SearchTradesResponse {

  private List<CryptoTradeInfo> trades;
  private long total;
  private int page;
  private int size;
}
