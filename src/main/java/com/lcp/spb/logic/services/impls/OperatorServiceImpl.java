package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.Operator;
import com.lcp.spb.bean.response.PageResponse;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.OperatorService;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OperatorServiceImpl extends AbstractMapperService implements OperatorService {

  @Override
  public Mono<PageResponse<Operator>> findPage(long page, long pageSize) {
    long safePage = Math.max(page, NumberUtils.LONG_ONE);
    long safePageSize = Math.max(pageSize, NumberUtils.LONG_ONE);
    long offset = (safePage - NumberUtils.LONG_ONE) * safePageSize;

    return fromBlocking(
        () -> {
          List<Operator> records = operatorMapper.findPage(offset, safePageSize);
          long total = operatorMapper.countAll();
          return new PageResponse<>(records, total, safePage, safePageSize);
        });
  }

  @Override
  public Mono<Operator> create(Operator operator) {
    return fromBlocking(
        () -> {
          operator.setId(null);
          operatorMapper.insert(operator);
          return operatorMapper.selectById(operator.getId());
        });
  }

  @Override
  public Mono<Operator> update(Operator operator) {
    return fromBlocking(
        () -> {
          operatorMapper.updateById(operator);
          return operatorMapper.selectById(operator.getId());
        });
  }
}
