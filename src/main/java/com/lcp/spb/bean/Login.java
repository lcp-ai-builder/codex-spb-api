package com.lcp.spb.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("login")
public class Login {

  @TableId(value = "user_id", type = IdType.INPUT) private String userId;

  private String password;

  public String getUserId() { return userId; }

  public void setUserId(String userId) { this.userId = userId; }

  public String getPassword() { return password; }

  public void setPassword(String password) { this.password = password; }
}
