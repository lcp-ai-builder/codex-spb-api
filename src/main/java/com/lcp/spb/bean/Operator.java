package com.lcp.spb.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
