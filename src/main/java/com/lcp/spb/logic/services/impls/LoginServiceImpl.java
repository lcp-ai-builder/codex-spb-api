package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.Login;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.LoginService;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service("loginService")
public class LoginServiceImpl extends AbstractMapperService implements LoginService {

  @Override
  public Mono<Login> create(Login login) {
    // 直接写入登录表，返回写入的实体
    return fromBlocking(() -> {
      loginMapper.insert(login);
      return login;
    });
  }

  @Override
  public Mono<Integer> insertLogin(Login login) {
    // 仅返回受影响行数，便于集成校验
    return fromBlocking(() -> loginMapper.insert(login));
  }

  @Override
  public Mono<Login> authenticate(String userId, String hashedPassword) {

    logger.info("userId:{}", userId);
    // 按 userId 查询并比对已计算好的 SHA-256 哈希
    // selectById 是阻塞调用：放到弹性线程池，再根据查到的数据决定发射或空
    return Mono.fromCallable(() -> loginMapper.selectById(userId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(login -> {
          // 未查到用户 -> 完整的空序列
          if (Objects.isNull(login)) {
            return Mono.empty();
          }
          // 口令哈希匹配 -> 返回用户，否则返回空
          if (Objects.equals(login.getPassword(), hashedPassword)) {
            return Mono.just(login);
          }
          return Mono.empty();
        });
  }
}
