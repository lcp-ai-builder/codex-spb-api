package com.lcp.spb.logic.services;

import com.lcp.spb.bean.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

  Flux<User> findAll();

  Mono<User> save(User user);
}
