package com.lcp.spb.logic.services;

import com.lcp.spb.bean.EsUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ElasticsearchUserService {

  Flux<EsUser> findAll ();

  Mono<EsUser> findById (String id);

  Mono<EsUser> save (EsUser user);

  Mono<EsUser> update (String id, EsUser user);

  Mono<Boolean> delete (String id);
}
