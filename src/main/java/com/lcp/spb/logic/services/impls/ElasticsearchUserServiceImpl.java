package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.EsUser;
import com.lcp.spb.logic.services.ElasticsearchUserService;
import com.lcp.spb.logic.services.BaseService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ElasticsearchUserServiceImpl extends BaseService implements ElasticsearchUserService {

  private static final String INDEX = "users";

  @Autowired
  private ElasticsearchClient elasticsearchClient;

  @Override
  public Flux<EsUser> findAll () {
    return fromBlocking( () -> elasticsearchClient.search(
        s -> s.index(INDEX).query(q -> q.matchAll(match -> match)), EsUser.class))
            .flatMapMany(searchResponse -> Flux.fromIterable(searchResponse.hits().hits()))
            .map(this::mapHit)
            .filter(Objects::nonNull);
  }

  @Override
  public Mono<EsUser> findById (String id) {
    return fromBlocking( () -> elasticsearchClient.get(
        g -> g.index(INDEX).id(id), EsUser.class))
            .flatMap(response -> response.found()
                ? Mono.justOrEmpty(attachId(response.source(), response.id()))
                : Mono.empty());
  }

  @Override
  public Mono<EsUser> save (EsUser user) {
    return fromBlocking( () -> elasticsearchClient.index(builder -> {
      builder.index(INDEX).document(user);
      if (user.getId() != null) {
        builder.id(user.getId());
      }
      return builder;
    }))
        .map(response -> copyWithId(user, response.id()));
  }

  @Override
  public Mono<EsUser> update (String id, EsUser user) {
    return findById(id)
        .flatMap(existing -> save(copyWithId(user, id)));
  }

  @Override
  public Mono<Boolean> delete (String id) {
    return fromBlocking( () -> elasticsearchClient.delete(d -> d.index(INDEX).id(id)))
        .map(DeleteResponse::result)
        .map(result -> result == Result.Deleted);
  }

  private EsUser mapHit (Hit<EsUser> hit) {
    return attachId(hit.source(), hit.id());
  }

  private EsUser attachId (EsUser user, String id) {
    if (user == null) {
      return null;
    }
    user.setId(id);
    return user;
  }

  private EsUser copyWithId (EsUser source, String id) {
    return new EsUser(id, source.getName(), source.getEmail());
  }
}
