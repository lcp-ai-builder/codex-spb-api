package com.lcp.spb.config;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * 统一为所有 RestController 方法执行前输出日志（方法名 + 参数），
 * 利用 Spring AOP 无需在各个 Controller 中显式写入日志。
 */
@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    /** 匹配所有 RestController 的公开方法 */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void anyRestControllerMethod () {}

    /** 方法执行前记录调用信息 */
    @Before("anyRestControllerMethod()")
    public void logBefore (JoinPoint joinPoint) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        String methodSignature = joinPoint.getSignature().toShortString();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(this::safeToString)
                .collect(Collectors.joining(", "));
        logger.info("Invoke controller: {} with args: {}", methodSignature, args);
    }

    private String safeToString (Object arg) {
        if (Objects.isNull(arg)) {
            return "null";
        }
        try {
            return arg.toString();
        } catch (Exception ex) {
            return arg.getClass().getSimpleName() + "(toString failed)";
        }
    }
}
