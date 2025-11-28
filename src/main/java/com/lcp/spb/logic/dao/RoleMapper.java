package com.lcp.spb.logic.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcp.spb.bean.Role;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

  List<Role> findPage(@Param("offset") long offset, @Param("limit") long limit);

  long countAll();

  int updateStatusById(@Param("roleId") Long roleId, @Param("status") String status);
}
