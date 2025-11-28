package com.lcp.spb.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lcp.spb.logic.services.LoginService;
import com.lcp.spb.logic.services.OperatorService;
import com.lcp.spb.logic.services.RoleService;
import com.lcp.spb.logic.services.UserService;

public abstract class AbstractController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected LoginService loginService;
    @Autowired
    protected OperatorService operatorService;
    @Autowired
    protected RoleService roleService;
    @Autowired
    protected UserService userService;

    protected String sha256(String value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to compute SHA-256 for userId", e);
            return StringUtils.EMPTY;
        }
    }
}
