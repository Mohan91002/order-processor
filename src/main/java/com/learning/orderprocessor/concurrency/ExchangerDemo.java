package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Demonstrates: Exchanger — two threads hand off a value at a sync point
@Component
public class ExchangerDemo {

    public Map<String, Object> run() throws Exception {
        Exchanger<String> ex = new Exchanger<>();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var fa = pool.submit(() -> ex.exchange("ping"));
            var fb = pool.submit(() -> ex.exchange("pong"));
            String aGot = fa.get(1, TimeUnit.SECONDS);
            String bGot = fb.get(1, TimeUnit.SECONDS);
            return Map.of("primitive", "Exchanger", "aReceived", aGot, "bReceived", bGot);
        } finally {
            pool.shutdownNow();
        }
    }
}
