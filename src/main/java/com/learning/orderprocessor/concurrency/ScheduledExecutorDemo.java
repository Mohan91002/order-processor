package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Demonstrates: ScheduledExecutorService — one-shot schedule + scheduleAtFixedRate
@Component
public class ScheduledExecutorDemo {

    public Map<String, Object> run() throws Exception {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        try {
            AtomicInteger ticks = new AtomicInteger();
            ScheduledFuture<?> recurring = scheduler.scheduleAtFixedRate(ticks::incrementAndGet, 0, 50, TimeUnit.MILLISECONDS);
            ScheduledFuture<String> oneShot = scheduler.schedule(() -> "fired", 200, TimeUnit.MILLISECONDS);

            String oneShotResult = oneShot.get(500, TimeUnit.MILLISECONDS);
            Thread.sleep(150);
            recurring.cancel(false);

            return Map.of(
                    "primitive", "ScheduledExecutorService",
                    "oneShot", oneShotResult,
                    "tickCount", ticks.get()
            );
        } finally {
            scheduler.shutdownNow();
        }
    }
}
