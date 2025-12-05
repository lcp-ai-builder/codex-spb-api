package com.lcp.spb.bean.trade;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class RecentHourTradeSummary {

  private long count;
  private BigDecimal totalAmount;
  private long windowStart;
  private long windowEnd;
  private boolean fallback;
}
