package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Demonstrates: ReentrantReadWriteLock — many concurrent readers, exclusive writer
@Component
public class ReadWriteLockDemo {

    public Map<String, Object> run() throws Exception {
        Map<String, Integer> shared = new HashMap<>();
        shared.put("k", 0);
        ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
        AtomicInteger reads = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(6);
        try {
            for (int i = 0; i < 4; i++) {
                pool.submit(() -> {
                    rw.readLock().lock();
                    try {
                        Integer ignored = shared.get("k");
                        reads.incrementAndGet();
                    } finally {
                        rw.readLock().unlock();
                    }
                });
            }
            pool.submit(() -> {
                rw.writeLock().lock();
                try {
                    shared.put("k", 99);
                } finally {
                    rw.writeLock().unlock();
                }
            });

            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            return Map.of("primitive", "ReentrantReadWriteLock", "reads", reads.get(), "finalValue", shared.get("k"));
        } finally {
            pool.shutdownNow();
        }
    }
}
