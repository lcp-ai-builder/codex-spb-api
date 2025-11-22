package com.lcp.spb.logic.services;

import com.lcp.spb.bean.Login;
import com.lcp.spb.logic.dao.LoginMapper;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class LoginService {

  private final LoginMapper loginMapper;

  public LoginService(LoginMapper loginMapper) {
    this.loginMapper = loginMapper;
  }

  public Mono<Login> createNewLoginInfo(Login login) {
    return Mono
        .fromCallable(() -> {
          loginMapper.insert(login);
          return login;
        })
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<Login> authenticate(Long userId, String hashedPassword) {
    return Mono.fromCallable(() -> loginMapper.selectById(userId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(login -> {
          if (login == null) {
            return Mono.empty();
          }
          if (Objects.equals(login.getPassword(), hashedPassword)) {
            return Mono.just(login);
          }
          return Mono.empty();
        });
  }
}
