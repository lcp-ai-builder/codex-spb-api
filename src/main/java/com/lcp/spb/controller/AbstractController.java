package com.lcp.spb.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 控制器抽象基类
 * 
 * <p>提供所有控制器共用的通用功能，包括：
 * <ul>
 *   <li>日志记录器：为子类提供统一的日志记录能力</li>
 *   <li>SHA-256 哈希计算：用于对敏感信息（如用户ID）进行加密处理</li>
 * </ul>
 * 
 * <p>所有具体的控制器类都应该继承此类，以复用这些通用功能。
 * 
 * @author lcp
 */
public abstract class AbstractController {

    /** 日志记录器，子类可直接使用 */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 计算字符串的 SHA-256 哈希值
     * 
     * <p>该方法用于对敏感信息进行单向加密，常用于用户ID等需要脱敏的场景。
     * 
     * <p>处理逻辑：
     * <ul>
     *   <li>如果输入为 null，返回空字符串</li>
     *   <li>使用 UTF-8 编码将字符串转换为字节数组</li>
     *   <li>计算 SHA-256 哈希值</li>
     *   <li>将哈希值转换为十六进制字符串（小写）</li>
     *   <li>如果计算过程中发生异常，记录错误日志并返回空字符串</li>
     * </ul>
     * 
     * @param value 待计算哈希值的字符串，可以为 null
     * @return SHA-256 哈希值的十六进制字符串（小写），如果输入为 null 或计算失败则返回空字符串
     */
    protected String sha256 (String value) {
        if (Objects.isNull(value)) {
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
