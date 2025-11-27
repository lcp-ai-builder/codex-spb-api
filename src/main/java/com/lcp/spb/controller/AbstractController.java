package com.lcp.spb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lcp.spb.logic.services.LoginService;
import com.lcp.spb.logic.services.RoleService;
import com.lcp.spb.logic.services.UserService;

public abstract class AbstractController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected LoginService loginService;
    @Autowired
    protected RoleService roleService;
    @Autowired
    protected UserService userService;
}
