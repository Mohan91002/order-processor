package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Demonstrates: 'volatile' for visibility of a flag; 'synchronized' block for mutual exclusion
@Component
public class SynchronizedVolatileDemo {

    static class Counter {
        private int count = 0;
        synchronized void inc() { count++; }
        synchronized int get() { return count; }
    }

    static class Stopper {
        private volatile boolean stopped = false;
        void stop() { stopped = true; }
        boolean isStopped() { return stopped; }
    }

    public Map<String, Object> run() throws Exception {
        Counter c = new Counter();
        Stopper s = new Stopper();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            pool.submit(() -> { while (!s.isStopped()) c.inc(); });
            Thread.sleep(50);
            s.stop();
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.SECONDS);
            return Map.of(
                    "primitive", "synchronized + volatile",
                    "countWhileRunning", c.get(),
                    "stopperSeenAsStopped", s.isStopped()
            );
        } finally {
            pool.shutdownNow();
        }
    }
}
