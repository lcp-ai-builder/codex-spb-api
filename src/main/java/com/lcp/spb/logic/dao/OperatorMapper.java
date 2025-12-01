package com.lcp.spb.logic.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcp.spb.bean.Operator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OperatorMapper extends BaseMapper<Operator> {

  int insertNewOperator(Operator operator);

  int updateIsOpenById(@Param("operatorId") Long operatorId, @Param("isOpen") Integer isOpen);
}
