package com.lcp.spb.bean;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperatorStatus {
  ENABLED("ENABLED"),
  DISABLED("DISABLED");

  @EnumValue
  @JsonValue
  private final String code;

  OperatorStatus(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
