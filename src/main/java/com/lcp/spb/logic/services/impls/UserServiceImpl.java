package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.User;
import com.lcp.spb.logic.dao.UserMapper;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserServiceImpl extends AbstractMapperService<UserMapper> implements UserService {

  public UserServiceImpl(UserMapper userMapper) {
    super(userMapper);
  }

  @Override
  public Flux<User> findAll() {
    return Flux.defer(() -> Flux.fromIterable(mapper.selectList(null)))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<User> save(User user) {
    return fromBlocking(() -> {
      mapper.insert(user);
      return user;
    });
  }
}
