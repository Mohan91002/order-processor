package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

// Demonstrates: AtomicInteger CAS, AtomicReference.updateAndGet, LongAdder for high-contention counters
@Component
public class AtomicsDemo {

    public Map<String, Object> run() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        LongAdder adder = new LongAdder();
        AtomicReference<String> last = new AtomicReference<>("none");

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            for (int i = 0; i < 1000; i++) {
                final int id = i;
                pool.submit(() -> {
                    counter.incrementAndGet();
                    adder.increment();
                    last.updateAndGet(prev -> "thread-" + Thread.currentThread().getId() + "-" + id);
                });
            }
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            return Map.of(
                    "primitive", "AtomicInteger / LongAdder / AtomicReference",
                    "atomicCounter", counter.get(),
                    "longAdder", adder.sum(),
                    "lastSeen", last.get()
            );
        } finally {
            pool.shutdownNow();
        }
    }
}
