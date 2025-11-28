package com.lcp.spb.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

@TableName("rules")
public class Role {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;
  private String name;
  private String code;
  private String description;
  private String status;

  @TableField("created_at")
  private LocalDate createdAt;

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  public String getCode() { return code; }

  public void setCode(String code) { this.code = code; }

  public String getDescription() { return description; }

  public void setDescription(String description) { this.description = description; }

  public String getStatus() { return status; }

  public void setStatus(String status) { this.status = status; }

  public LocalDate getCreatedAt() { return createdAt; }

  public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
