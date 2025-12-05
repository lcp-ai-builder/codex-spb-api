package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.EsUser;
import com.lcp.spb.logic.services.ElasticsearchUserService;
import com.lcp.spb.logic.services.BaseService;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ElasticsearchUserServiceImpl extends BaseService implements ElasticsearchUserService {

  // 用户索引的 CRUD 服务，封装 ES 调用
  private static final String INDEX = "users";

  // 查询所有用户文档
  @Override
  public Flux<EsUser> findAll () {
    return fromBlocking( () -> elasticsearchClient.search(
        searchRequest -> searchRequest.index(INDEX).query(queryBuilder -> queryBuilder.matchAll(matchAll -> matchAll)),
        EsUser.class))
            .flatMapMany(searchResponse -> Flux.fromIterable(
                Optional.ofNullable(searchResponse.hits())
                    .map(searchHits -> searchHits.hits())
                    .orElseGet(java.util.List::of)))
            .map(this::mapHit)
            .filter(Objects::nonNull);
  }

  // 根据 ID 查询用户
  @Override
  public Mono<EsUser> findById (String id) {
    return fromBlocking( () -> elasticsearchClient.get(
        g -> g.index(INDEX).id(id), EsUser.class))
            .flatMap(response -> response.found()
                ? Mono.justOrEmpty(attachId(response.source(), response.id()))
                : Mono.empty());
  }

  // 新增或覆盖保存用户
  @Override
  public Mono<EsUser> save (EsUser user) {
    return fromBlocking( () -> elasticsearchClient.index(builder -> {
      builder.index(INDEX).document(user);
      if (Objects.nonNull(user.getId())) {
        builder.id(user.getId());
      }
      return builder;
    }))
        .map(response -> copyWithId(user, response.id()));
  }

  // 根据 ID 更新用户信息（先查后存）
  @Override
  public Mono<EsUser> update (String id, EsUser user) {
    return findById(id)
        .flatMap(existing -> save(copyWithId(user, id)));
  }

  // 删除用户，返回是否成功
  @Override
  public Mono<Boolean> delete (String id) {
    return fromBlocking( () -> elasticsearchClient.delete(d -> d.index(INDEX).id(id)))
        .map(DeleteResponse::result)
        .map(result -> result == Result.Deleted);
  }

  // 将命中结果转换为实体，并附带文档 ID
  private EsUser mapHit (Hit<EsUser> hit) {
    return attachId(hit.source(), hit.id());
  }

  private EsUser attachId (EsUser user, String id) {
    if (Objects.isNull(user)) {
      return null;
    }
    user.setId(id);
    return user;
  }

  private EsUser copyWithId (EsUser source, String id) {
    return new EsUser(id, source.getName(), source.getEmail());
  }
}
