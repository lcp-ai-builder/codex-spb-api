package com.lcp.spb.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("rules")
public class Role {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;
  private String name;
  private String code;
  private String description;

  @TableField("is_open")
  private Integer isOpen;

  @TableField("created_at")
  private LocalDate createdAt;
}
