package com.lcp.spb.logic.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcp.spb.bean.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

  int updateStatusById(@Param("roleId") Long roleId, @Param("status") String status);
}
