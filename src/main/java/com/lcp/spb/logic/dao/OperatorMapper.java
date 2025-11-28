package com.lcp.spb.logic.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcp.spb.bean.Operator;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OperatorMapper extends BaseMapper<Operator> {

  List<Operator> findPage(@Param("offset") long offset, @Param("limit") long limit);

  long countAll();

  int insertNewOperator(Operator operator);
}
