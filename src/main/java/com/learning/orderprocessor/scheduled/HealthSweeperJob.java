package com.learning.orderprocessor.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Demonstrates: @Scheduled fixedDelay (run N seconds after previous finishes)
@Component
public class HealthSweeperJob {

    private static final Logger log = LoggerFactory.getLogger(HealthSweeperJob.class);

    @Scheduled(fixedDelayString = "PT60S", initialDelayString = "PT15S")
    public void sweep() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        log.info("[scheduled sweep] threads={}, heapUsedMB={}", Thread.activeCount(), used);
    }
}
