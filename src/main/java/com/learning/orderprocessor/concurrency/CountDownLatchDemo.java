package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Demonstrates: CountDownLatch — one-shot gate that waits for N workers to complete
@Component
public class CountDownLatchDemo {

    public Map<String, Object> run() throws Exception {
        int workers = 5;
        CountDownLatch latch = new CountDownLatch(workers);
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i < workers; i++) {
                pool.submit(() -> {
                    try { Thread.sleep(80); } catch (InterruptedException ignored) {}
                    latch.countDown();
                });
            }
            boolean done = latch.await(2, TimeUnit.SECONDS);
            return Map.of("primitive", "CountDownLatch", "allDone", done, "elapsedMs", System.currentTimeMillis() - start);
        } finally {
            pool.shutdownNow();
        }
    }
}
