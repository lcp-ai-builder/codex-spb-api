package com.lcp.spb.logic.services;

import com.lcp.spb.bean.Login;
import reactor.core.publisher.Mono;

public interface LoginService {

  Mono<Login> create(Login login);

  Mono<Integer> insertLogin(Login login);

  Mono<Login> authenticate(String userId, String hashedPassword);
}
