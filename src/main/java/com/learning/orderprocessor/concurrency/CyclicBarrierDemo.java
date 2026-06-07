package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Demonstrates: CyclicBarrier — N parties rendezvous at a barrier, barrier action runs once each cycle
@Component
public class CyclicBarrierDemo {

    public Map<String, Object> run() throws Exception {
        AtomicInteger barrierFiredTimes = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(3, barrierFiredTimes::incrementAndGet);
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            for (int round = 0; round < 2; round++) {
                for (int p = 0; p < 3; p++) {
                    pool.submit(() -> {
                        try {
                            Thread.sleep(50);
                            barrier.await(1, TimeUnit.SECONDS);
                        } catch (Exception ignored) {}
                    });
                }
            }
            pool.shutdown();
            pool.awaitTermination(3, TimeUnit.SECONDS);
            return Map.of("primitive", "CyclicBarrier", "barrierTrips", barrierFiredTimes.get());
        } finally {
            pool.shutdownNow();
        }
    }
}
