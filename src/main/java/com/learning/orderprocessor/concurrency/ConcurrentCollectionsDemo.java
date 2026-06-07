package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Demonstrates: ConcurrentHashMap (compute/merge), CopyOnWriteArrayList (read-mostly)
@Component
public class ConcurrentCollectionsDemo {

    public Map<String, Object> run() throws Exception {
        ConcurrentHashMap<String, Integer> tallies = new ConcurrentHashMap<>();
        CopyOnWriteArrayList<String> events = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            for (int i = 0; i < 1000; i++) {
                final int id = i;
                pool.submit(() -> {
                    tallies.merge("hits", 1, Integer::sum);
                    if (id % 100 == 0) events.add("milestone-" + id);
                });
            }
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            return Map.of(
                    "primitive", "ConcurrentHashMap + CopyOnWriteArrayList",
                    "hits", tallies.get("hits"),
                    "milestones", events
            );
        } finally {
            pool.shutdownNow();
        }
    }
}
