package com.learning.orderprocessor.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Demonstrates: Spring AOP — @Before / @AfterReturning / @AfterThrowing advice on all service methods
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.learning.orderprocessor.service..*(..))")
    public void before(JoinPoint jp) {
        log.debug("[AOP enter] {}", jp.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "execution(* com.learning.orderprocessor.service..*(..))", returning = "result")
    public void afterReturning(JoinPoint jp, Object result) {
        log.debug("[AOP exit ] {} -> {}", jp.getSignature().toShortString(),
                result == null ? "null" : result.getClass().getSimpleName());
    }

    @AfterThrowing(pointcut = "execution(* com.learning.orderprocessor.service..*(..))", throwing = "ex")
    public void afterThrowing(JoinPoint jp, Throwable ex) {
        log.warn("[AOP throw] {} threw {}: {}", jp.getSignature().toShortString(),
                ex.getClass().getSimpleName(), ex.getMessage());
    }
}
