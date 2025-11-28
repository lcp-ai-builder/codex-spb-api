package com.lcp.spb.logic.services;

import com.lcp.spb.bean.Operator;
import com.lcp.spb.bean.response.PageResponse;
import reactor.core.publisher.Mono;

public interface OperatorService {

  Mono<PageResponse<Operator>> findPage(long page, long pageSize);

  Mono<Operator> create(Operator operator);

  Mono<Operator> update(Operator operator);
}
