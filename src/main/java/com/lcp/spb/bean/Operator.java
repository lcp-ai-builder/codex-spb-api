package com.lcp.spb.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("operators")
public class Operator {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("operator_no")
  private String operatorNo;

  private String name;

  @TableField("login_name")
  private String loginName;

  @TableField("login_password")
  private String loginPassword;

  private String phone;
  private String email;

  @TableField("role_id")
  private Long roleId;

  private OperatorStatus status;

  @TableField("created_at")
  private LocalDateTime createdAt;

  @TableField("last_login_at")
  private LocalDateTime lastLoginAt;

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public String getOperatorNo() { return operatorNo; }

  public void setOperatorNo(String operatorNo) { this.operatorNo = operatorNo; }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  public String getLoginName() { return loginName; }

  public void setLoginName(String loginName) { this.loginName = loginName; }

  public String getLoginPassword() { return loginPassword; }

  public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }

  public String getPhone() { return phone; }

  public void setPhone(String phone) { this.phone = phone; }

  public String getEmail() { return email; }

  public void setEmail(String email) { this.email = email; }

  public Long getRoleId() { return roleId; }

  public void setRoleId(Long roleId) { this.roleId = roleId; }

  public OperatorStatus getStatus() { return status; }

  public void setStatus(OperatorStatus status) { this.status = status; }

  public LocalDateTime getCreatedAt() { return createdAt; }

  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getLastLoginAt() { return lastLoginAt; }

  public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
