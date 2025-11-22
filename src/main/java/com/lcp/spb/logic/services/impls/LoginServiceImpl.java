package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.Login;
import com.lcp.spb.logic.dao.LoginMapper;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.LoginService;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class LoginServiceImpl extends AbstractMapperService<LoginMapper> implements LoginService {

  public LoginServiceImpl(LoginMapper loginMapper) {
    super(loginMapper);
  }

  @Override
  public Mono<Login> create(Login login) {
    return fromBlocking(() -> {
      mapper.insert(login);
      return login;
    });
  }

  @Override
  public Mono<Integer> insertLogin(Login login) {
    return fromBlocking(() -> mapper.insert(login));
  }

  @Override
  public Mono<Login> authenticate(String userId, String hashedPassword) {
    return Mono.fromCallable(() -> mapper.selectById(userId))
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
