package com.lcp.spb.controller;

import com.lcp.spb.bean.EsUser;
import com.lcp.spb.logic.services.ElasticsearchUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Elasticsearch 用户数据控制器
 * 
 * <p>提供用户数据的完整 CRUD（创建、读取、更新、删除）操作接口。
 * 所有用户数据存储在 Elasticsearch 的 "users" 索引中。
 * 
 * <p>功能特性：
 * <ul>
 *   <li>查询所有用户：返回所有用户列表</li>
 *   <li>根据ID查询用户：支持单个用户查询</li>
 *   <li>创建用户：新增用户记录</li>
 *   <li>更新用户：根据ID更新用户信息</li>
 *   <li>删除用户：根据ID删除用户记录</li>
 * </ul>
 * 
 * <p>所有接口均基于响应式编程模型（Reactor），支持非阻塞异步处理。
 * 
 * <p>API 路径前缀：/es/users
 * 
 * @author lcp
 */
@RestController @RequestMapping("/es/users")
public class ElasticsearchUserController extends AbstractController {

  /** 用户服务，负责用户数据的业务逻辑处理 */
  @Autowired
  private ElasticsearchUserService elasticsearchUserService;

  /**
   * 查询所有用户
   * 
   * <p>从 Elasticsearch 中查询所有用户记录，返回用户列表。
   * 
   * <p>请求方式：GET /es/users
   * 
   * @return Flux 流式返回所有用户对象
   */
  @GetMapping
  public Flux<EsUser> listUsers () {
    return elasticsearchUserService.findAll();
  }

  /**
   * 根据ID查询用户
   * 
   * <p>根据用户ID从 Elasticsearch 中查询对应的用户信息。
   * 如果用户不存在，返回 404 Not Found。
   * 
   * <p>请求方式：GET /es/users/{id}
   * 
   * @param id 用户ID，作为路径变量
   * @return Mono 包装的 ResponseEntity，包含用户对象（如果存在）或 404 状态码
   */
  @GetMapping("/{id}")
  public Mono<ResponseEntity<EsUser>> getUser (@PathVariable String id) {
    return elasticsearchUserService
        .findById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  /**
   * 创建用户
   * 
   * <p>在 Elasticsearch 中创建新的用户记录。如果请求体中包含用户ID，
   * 则使用该ID作为文档ID；否则由 Elasticsearch 自动生成。
   * 
   * <p>请求方式：POST /es/users
   * 
   * @param user 用户对象，包含用户的基本信息（id、name、email）
   * @return Mono 包装的用户对象，包含保存后的用户信息（包括生成的ID）
   */
  @PostMapping
  public Mono<EsUser> createUser (@RequestBody EsUser user) {
    return elasticsearchUserService.save(user);
  }

  /**
   * 更新用户信息
   * 
   * <p>根据用户ID更新用户信息。如果用户不存在，返回 404 Not Found。
   * 更新操作会先查询用户是否存在，然后保存更新后的信息。
   * 
   * <p>请求方式：PUT /es/users/{id}
   * 
   * @param id 用户ID，作为路径变量
   * @param user 更新后的用户对象，包含新的用户信息
   * @return Mono 包装的 ResponseEntity，包含更新后的用户对象（如果存在）或 404 状态码
   */
  @PutMapping("/{id}")
  public Mono<ResponseEntity<EsUser>> updateUser (
      @PathVariable String id, @RequestBody EsUser user) {
    return elasticsearchUserService
        .update(id, user)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  /**
   * 删除用户
   * 
   * <p>根据用户ID从 Elasticsearch 中删除用户记录。
   * 如果用户不存在，返回 404 Not Found；删除成功返回 204 No Content。
   * 
   * <p>请求方式：DELETE /es/users/{id}
   * 
   * @param id 用户ID，作为路径变量
   * @return Mono 包装的 ResponseEntity，删除成功返回 204，用户不存在返回 404
   */
  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> deleteUser (@PathVariable String id) {
    return elasticsearchUserService
        .delete(id)
        .map(deleted -> deleted
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build());
  }
}
