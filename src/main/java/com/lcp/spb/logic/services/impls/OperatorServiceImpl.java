package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.Operator;
import com.lcp.spb.bean.response.PageResponse;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.OperatorService;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OperatorServiceImpl extends AbstractMapperService implements OperatorService {

  @Override
  public Mono<PageResponse<Operator>> findPage(long page, long pageSize) {
    var bounds = normalizePage(page, pageSize);

    return fromBlocking(
        () -> {
          List<Operator> records = operatorMapper.findPage(bounds.getOffset(), bounds.getPageSize());
          long total = operatorMapper.countAll();
          return new PageResponse<>(records, total, bounds.getPage(), bounds.getPageSize());
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
