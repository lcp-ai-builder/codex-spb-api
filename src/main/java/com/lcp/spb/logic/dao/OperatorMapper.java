package com.lcp.spb.logic.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcp.spb.bean.Operator;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperatorMapper extends BaseMapper<Operator> {

  int insertNewOperator(Operator operator);
}
