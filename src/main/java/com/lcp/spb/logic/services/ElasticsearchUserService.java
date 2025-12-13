package com.lcp.spb.logic.services;

import com.lcp.spb.bean.EsUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Elasticsearch 用户服务接口
 * 
 * <p>定义用户数据的完整 CRUD 操作，包括：
 * <ul>
 *   <li>查询所有用户：返回所有用户列表</li>
 *   <li>根据ID查询用户：支持单个用户查询</li>
 *   <li>创建用户：新增用户记录</li>
 *   <li>更新用户：根据ID更新用户信息</li>
 *   <li>删除用户：根据ID删除用户记录</li>
 * </ul>
 * 
 * <p>所有方法均返回响应式类型（Mono 或 Flux），支持非阻塞异步处理。
 * 所有用户数据存储在 Elasticsearch 的 "users" 索引中。
 * 
 * @author lcp
 */
public interface ElasticsearchUserService {

  /**
   * 查询所有用户
   * 
   * @return Flux 流式返回所有用户对象
   */
  Flux<EsUser> findAll ();

  /**
   * 根据ID查询用户
   * 
   * @param id 用户ID
   * @return Mono 包装的用户对象，如果用户不存在则返回空 Mono
   */
  Mono<EsUser> findById (String id);

  /**
   * 保存用户（新增或更新）
   * 
   * <p>如果用户对象中包含ID，则使用该ID作为文档ID；否则由 Elasticsearch 自动生成。
   * 
   * @param user 用户对象
   * @return Mono 包装的用户对象，包含保存后的用户信息（包括生成的ID）
   */
  Mono<EsUser> save (EsUser user);

  /**
   * 更新用户信息
   * 
   * <p>根据用户ID更新用户信息。如果用户不存在，操作会失败。
   * 
   * @param id 用户ID
   * @param user 更新后的用户对象
   * @return Mono 包装的用户对象，如果用户不存在则返回空 Mono
   */
  Mono<EsUser> update (String id, EsUser user);

  /**
   * 删除用户
   * 
   * @param id 用户ID
   * @return Mono 包装的布尔值，true 表示删除成功，false 表示用户不存在
   */
  Mono<Boolean> delete (String id);
}
