package com.lcp.spb.logic.services;

import com.lcp.spb.bean.Login;
import reactor.core.publisher.Mono;

public interface LoginService {

  // 新建登录信息（写入 login 表）
  Mono<Login> create(Login login);

  // 仅返回插入影响行数，便于集成测试校验
  Mono<Integer> insertLogin(Login login);

  // 校验用户 ID 与已计算好的口令哈希
  Mono<Login> authenticate(String userId, String hashedPassword);
}
