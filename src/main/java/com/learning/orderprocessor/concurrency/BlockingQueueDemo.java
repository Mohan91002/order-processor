package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Demonstrates: ArrayBlockingQueue (bounded) — backpressure between producer and consumer
@Component
public class BlockingQueueDemo {

    public Map<String, Object> run() throws Exception {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(4);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Integer> consumed = new ArrayList<>();
        try {
            pool.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    try { queue.put(i); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
            pool.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    try { consumed.add(queue.take()); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
            pool.shutdown();
            pool.awaitTermination(3, TimeUnit.SECONDS);
            return Map.of("primitive", "ArrayBlockingQueue", "capacity", 4, "consumed", consumed);
        } finally {
            pool.shutdownNow();
        }
    }
}
