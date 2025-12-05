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

@Getter @Setter @NoArgsConstructor
public class UserDoc {

  @JsonProperty("user_name")
  private String userName;

  private Integer age;
  private String email;

  @JsonProperty("is_vip")
  private Boolean isVip;

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
