package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.EsUser;
import com.lcp.spb.logic.services.ElasticsearchUserService;
import com.lcp.spb.logic.services.BaseService;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Elasticsearch 用户服务实现类
 * 
 * <p>实现用户数据的完整 CRUD 操作，所有操作均基于 Elasticsearch 的 "users" 索引。
 * 
 * <p>功能特性：
 * <ul>
 *   <li>查询所有用户：使用 match_all 查询获取所有用户</li>
 *   <li>根据ID查询：使用 get API 直接获取文档</li>
 *   <li>保存用户：支持新增和更新（如果ID存在则更新）</li>
 *   <li>更新用户：先查询后保存的方式实现更新</li>
 *   <li>删除用户：根据ID删除文档</li>
 * </ul>
 * 
 * <p>所有操作均通过 {@link BaseService#fromBlocking(Callable)} 方法包装为响应式操作，
 * 确保不会阻塞事件循环线程。
 * 
 * @author lcp
 */
@Service
public class ElasticsearchUserServiceImpl extends BaseService implements ElasticsearchUserService {

  /** Elasticsearch 索引名称，用于存储用户数据 */
  private static final String INDEX = "users";

  /**
   * 查询所有用户文档
   * 
   * <p>使用 match_all 查询获取索引中的所有用户记录。
   * 
   * @return Flux 流式返回所有用户对象
   */
  @Override
  public Flux<EsUser> findAll () {
    return fromBlocking( () -> elasticsearchClient.search(
        searchRequest -> searchRequest.index(INDEX)
                .query(queryBuilder -> queryBuilder.matchAll(matchAll -> matchAll)),
        EsUser.class))
            .flatMapMany(this::extractHits)
            .map(this::mapHit)
            .filter(Objects::nonNull);
  }

  /**
   * 从 Elasticsearch 查询响应中提取命中结果
   * 
   * @param response 查询响应
   * @return Flux 流式返回命中结果
   */
  private Flux<Hit<EsUser>> extractHits (
          co.elastic.clients.elasticsearch.core.SearchResponse<EsUser> response) {
    return Flux.fromIterable(
            Optional.ofNullable(response.hits())
                    .map(searchHits -> searchHits.hits())
                    .orElseGet(java.util.List::of));
  }

  /**
   * 根据 ID 查询用户
   * 
   * <p>使用 Elasticsearch 的 get API 直接根据文档ID获取用户信息。
   * 
   * @param id 用户ID
   * @return Mono 包装的用户对象，如果用户不存在则返回空 Mono
   */
  @Override
  public Mono<EsUser> findById (String id) {
    return fromBlocking( () -> elasticsearchClient.get(
        g -> g.index(INDEX).id(id), EsUser.class))
            .flatMap(response -> response.found()
                ? Mono.justOrEmpty(attachId(response.source(), response.id()))
                : Mono.empty());
  }

  /**
   * 新增或覆盖保存用户
   * 
   * <p>将用户信息保存到 Elasticsearch 索引中。如果用户对象中包含ID，
   * 则使用该ID作为文档ID（可用于更新操作）；否则由 Elasticsearch 自动生成。
   * 
   * <p>保存成功后，会将 Elasticsearch 返回的文档ID回填到用户对象中。
   * 
   * @param user 用户对象，包含用户的基本信息
   * @return Mono 包装的用户对象，包含保存后的用户信息（包括生成的ID）
   */
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

  /**
   * 根据 ID 更新用户信息
   * 
   * <p>更新操作采用先查询后保存的方式：
   * <ol>
   *   <li>先根据ID查询用户是否存在</li>
   *   <li>如果存在，则保存更新后的用户信息</li>
   *   <li>如果不存在，则返回空 Mono</li>
   * </ol>
   * 
   * @param id 用户ID
   * @param user 更新后的用户对象，包含新的用户信息
   * @return Mono 包装的用户对象，如果用户不存在则返回空 Mono
   */
  @Override
  public Mono<EsUser> update (String id, EsUser user) {
    return findById(id)
        .flatMap(existing -> save(copyWithId(user, id)));
  }

  /**
   * 删除用户
   * 
   * <p>根据用户ID从 Elasticsearch 索引中删除用户文档。
   * 
   * @param id 用户ID
   * @return Mono 包装的布尔值，true 表示删除成功，false 表示用户不存在
   */
  @Override
  public Mono<Boolean> delete (String id) {
    return fromBlocking( () -> elasticsearchClient.delete(d -> d.index(INDEX).id(id)))
        .map(DeleteResponse::result)
        .map(result -> result == Result.Deleted);
  }

  /**
   * 将 Elasticsearch 查询命中结果转换为用户实体，并附带文档ID
   * 
   * @param hit Elasticsearch 查询命中结果
   * @return 包含ID的用户对象
   */
  private EsUser mapHit (Hit<EsUser> hit) {
    return attachId(hit.source(), hit.id());
  }

  /**
   * 将文档ID附加到用户对象
   * 
   * <p>如果用户对象不为空，则将文档ID设置到用户对象中。
   * 
   * @param user 用户对象，可能为 null
   * @param id 文档ID
   * @return 包含ID的用户对象，如果输入用户为 null 则返回 null
   */
  private EsUser attachId (EsUser user, String id) {
    if (Objects.isNull(user)) {
      return null;
    }
    user.setId(id);
    return user;
  }

  /**
   * 复制用户对象并设置新的ID
   * 
   * <p>创建一个新的用户对象，使用指定的ID和源对象的其他属性。
   * 
   * @param source 源用户对象
   * @param id 新的用户ID
   * @return 新的用户对象，包含指定的ID和源对象的其他属性
   */
  private EsUser copyWithId (EsUser source, String id) {
    return new EsUser(id, source.getName(), source.getEmail());
  }
}
