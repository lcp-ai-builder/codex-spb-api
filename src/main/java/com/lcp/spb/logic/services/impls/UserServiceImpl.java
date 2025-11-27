package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.User;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service("userServices")
public class UserServiceImpl extends AbstractMapperService implements UserService {

  @Override
  public Flux<User> findAll() {
    // MyBatis 查询是阻塞的，转到弹性线程池
    return Flux.defer(() -> Flux.fromIterable(userMapper.selectList(null)))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<User> save(User user) {
    // 插入后返回同一个实体
    return fromBlocking(() -> {
      userMapper.insert(user);
      return user;
    });
  }
}
