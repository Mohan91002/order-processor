package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Demonstrates: Phaser — flexible multi-phase rendezvous (parties can register/deregister between phases)
@Component
public class PhaserDemo {

    public Map<String, Object> run() throws Exception {
        Phaser phaser = new Phaser(1); // main thread registered
        AtomicInteger completed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            for (int i = 0; i < 3; i++) {
                phaser.register();
                pool.submit(() -> {
                    for (int phase = 0; phase < 3; phase++) {
                        try { Thread.sleep(30); } catch (InterruptedException ignored) {}
                        phaser.arriveAndAwaitAdvance();
                    }
                    completed.incrementAndGet();
                    phaser.arriveAndDeregister();
                });
            }
            for (int phase = 0; phase < 3; phase++) {
                phaser.arriveAndAwaitAdvance();
            }
            phaser.arriveAndDeregister();
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            return Map.of("primitive", "Phaser", "phasesCompleted", phaser.getPhase(), "workers", completed.get());
        } finally {
            pool.shutdownNow();
        }
    }
}
