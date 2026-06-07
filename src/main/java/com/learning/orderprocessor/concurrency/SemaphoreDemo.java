package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Demonstrates: Semaphore as a fixed-size resource pool / rate limiter
@Component
public class SemaphoreDemo {

    public Map<String, Object> run() throws Exception {
        Semaphore gate = new Semaphore(2);
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            for (int i = 0; i < 8; i++) {
                pool.submit(() -> {
                    gate.acquire();
                    try {
                        int now = inFlight.incrementAndGet();
                        peak.accumulateAndGet(now, Math::max);
                        Thread.sleep(80);
                    } finally {
                        inFlight.decrementAndGet();
                        gate.release();
                    }
                    return null;
                });
            }
            pool.shutdown();
            pool.awaitTermination(3, TimeUnit.SECONDS);
            return Map.of("primitive", "Semaphore", "permits", 2, "observedPeakInFlight", peak.get());
        } finally {
            pool.shutdownNow();
        }
    }
}
