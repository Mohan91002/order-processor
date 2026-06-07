package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Demonstrates: ThreadLocal — per-thread state that survives across nested method calls
@Component
public class ThreadLocalDemo {

    private static final ThreadLocal<String> CONTEXT = ThreadLocal.withInitial(() -> "anon");

    public Map<String, Object> run() throws Exception {
        ConcurrentHashMap<String, String> observed = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            for (int i = 0; i < 3; i++) {
                final String user = "user-" + i;
                pool.submit(() -> {
                    try {
                        CONTEXT.set(user);
                        nested(observed);
                    } finally {
                        CONTEXT.remove(); // always clean up to avoid leaking into pooled threads
                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            return Map.of("primitive", "ThreadLocal", "perThreadContext", observed);
        } finally {
            pool.shutdownNow();
        }
    }

    private void nested(ConcurrentHashMap<String, String> out) {
        out.put(Thread.currentThread().getName(), CONTEXT.get());
    }
}
