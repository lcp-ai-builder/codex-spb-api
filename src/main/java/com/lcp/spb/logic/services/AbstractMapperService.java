package com.lcp.spb.logic.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lcp.spb.logic.dao.LoginMapper;
import com.lcp.spb.logic.dao.RoleMapper;
import com.lcp.spb.logic.dao.UserMapper;

public abstract class AbstractMapperService extends BaseService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected LoginMapper loginMapper;
    @Autowired
    protected RoleMapper roleMapper;
    @Autowired
    protected UserMapper userMapper;

    // 子类直接复用持久层 mapper
}
