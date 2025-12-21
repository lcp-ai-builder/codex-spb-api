package com.lcp.spb.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户文档实体类
 * 
 * <p>表示用户信息的文档对象，使用自定义的 JSON 属性映射。
 * 该类重写了 equals、hashCode 和 toString 方法，使用 Apache Commons Lang 的工具类实现。
 * 
 * <p>字段说明：
 * <ul>
 *   <li>userName：用户名称，JSON 字段名为 "user_name"</li>
 *   <li>age：用户年龄</li>
 *   <li>email：用户邮箱</li>
 *   <li>isVip：是否为VIP用户，JSON 字段名为 "is_vip"</li>
 *   <li>description：用户描述信息</li>
 * </ul>
 * 
 * @author lcp
 */
@Getter @Setter @NoArgsConstructor
public class UserDoc {

  /** 用户名称，JSON 序列化时使用 "user_name" 作为字段名 */
  @JsonProperty("user_name")
  private String userName;

  /** 用户年龄 */
  private Integer age;
  /** 用户邮箱 */
  private String email;

  /** 是否为VIP用户，JSON 序列化时使用 "is_vip" 作为字段名 */
  @JsonProperty("is_vip")
  private Boolean isVip;

  /** 用户描述信息 */
  private String description;

  @Override
  public boolean equals (Object obj) {
    if (this == obj) {
      return true;
    }
    if (Objects.isNull(obj) || getClass() != obj.getClass()) {
      return false;
    }
    UserDoc other = (UserDoc) obj;
    return new EqualsBuilder()
        .append(userName, other.userName)
        .append(age, other.age)
        .append(email, other.email)
        .append(isVip, other.isVip)
        .append(description, other.description)
        .isEquals();
  }

  @Override
  public int hashCode () {
    return new HashCodeBuilder(17, 37)
        .append(userName)
        .append(age)
        .append(email)
        .append(isVip)
        .append(description)
        .toHashCode();
  }

  @Override
  public String toString () {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("userName", userName)
        .append("age", age)
        .append("email", email)
        .append("isVip", isVip)
        .append("description", description)
        .toString();
  }
}
