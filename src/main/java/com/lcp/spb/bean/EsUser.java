package com.lcp.spb.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * Elasticsearch 用户实体类
 * 
 * <p>表示存储在 Elasticsearch 中的用户信息。
 * 使用 Spring Data Elasticsearch 注解标记索引名称和文档ID。
 * 
 * <p>字段说明：
 * <ul>
 *   <li>id：用户唯一标识，在 Elasticsearch 中作为文档ID</li>
 *   <li>name：用户姓名</li>
 *   <li>email：用户邮箱</li>
 * </ul>
 * 
 * <p>索引名称：users
 * 
 * @author lcp
 */
@Data @NoArgsConstructor @AllArgsConstructor @Document(indexName = "users")
public class EsUser {

  /** 用户唯一标识，在 Elasticsearch 中作为文档ID */
  @Id
  private String id;
  /** 用户姓名 */
  private String name;
  /** 用户邮箱 */
  private String email;
}
