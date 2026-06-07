package com.learning.orderprocessor.aop;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// Demonstrates: @Around advice that times all controllers + ships timings to Micrometer
@Aspect
@Component
public class TimingAspect {

    private static final Logger log = LoggerFactory.getLogger(TimingAspect.class);
    private final MeterRegistry meters;

    public TimingAspect(MeterRegistry meters) {
        this.meters = meters;
    }

    @Around("execution(* com.learning.orderprocessor.controller..*(..)) || execution(* com.learning.orderprocessor.concurrency.DemoController.*(..))")
    public Object timeIt(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long elapsedNs = System.nanoTime() - start;
            String tag = pjp.getSignature().toShortString();
            meters.timer("controller.timing", "method", tag)
                    .record(elapsedNs, TimeUnit.NANOSECONDS);
            log.debug("[timing] {} took {} ms", tag, elapsedNs / 1_000_000);
        }
    }
}
