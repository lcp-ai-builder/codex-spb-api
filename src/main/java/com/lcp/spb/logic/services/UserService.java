package com.lcp.spb.logic.services;

import com.lcp.spb.bean.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

  // 查询全部用户（阻塞调用包裹为 Flux）
  Flux<User> findAll();

  // 保存用户（阻塞插入包裹为 Mono）
  Mono<User> save(User user);
}
