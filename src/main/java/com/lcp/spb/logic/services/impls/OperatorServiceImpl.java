package com.lcp.spb.logic.services.impls;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcp.spb.bean.Operator;
import com.lcp.spb.bean.response.PageResponse;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.OperatorService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OperatorServiceImpl extends AbstractMapperService implements OperatorService {

  @Override
  public Mono<PageResponse<Operator>> findPage(long page, long pageSize) {
    var bounds = normalizePage(page, pageSize);

    return fromBlocking(
        () -> {
          Page<Operator> pageRequest = new Page<>(bounds.getPage(), bounds.getPageSize());
          Page<Operator> resultPage = operatorMapper.selectPage(pageRequest, null);
          return new PageResponse<>(
              resultPage.getRecords(),
              resultPage.getTotal(),
              resultPage.getCurrent(),
              resultPage.getSize());
        });
  }

  @Override
  public Mono<Operator> create(Operator operator) {
    return fromBlocking(
        () -> {
          operator.setId(null);
          operatorMapper.insertNewOperator(operator);
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

  @Override
  public Mono<Operator> updateIsOpen(Long operatorId, Integer isOpen) {
    return fromBlocking(
        () -> {
          operatorMapper.updateIsOpenById(operatorId, isOpen);
          return operatorMapper.selectById(operatorId);
        });
  }
}
