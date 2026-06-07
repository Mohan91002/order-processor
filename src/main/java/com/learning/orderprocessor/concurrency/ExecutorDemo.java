package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

// Demonstrates: Runnable + ExecutorService submit/invokeAll + graceful shutdown
@Component
public class ExecutorDemo {

    public Map<String, Object> run() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                final int id = i;
                futures.add(pool.submit(() -> "task-" + id + " on " + Thread.currentThread().getName()));
            }
            List<String> results = new ArrayList<>();
            for (Future<String> f : futures) results.add(f.get(2, TimeUnit.SECONDS));
            return Map.of("tool", "ExecutorService.newFixedThreadPool", "results", results);
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(2, TimeUnit.SECONDS)) pool.shutdownNow();
        }
    }
}
