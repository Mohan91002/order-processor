package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Demonstrates: CompletableFuture composition (supplyAsync, thenApply, thenCombine, allOf, exceptionally)
@Component
public class CompletableFutureDemo {

    public Map<String, Object> run() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> slow(2), pool);
            CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> slow(3), pool);

            CompletableFuture<Integer> sum = a.thenCombine(b, Integer::sum);
            CompletableFuture<String> labelled = sum.thenApply(s -> "sum=" + s);

            CompletableFuture<Integer> failing = CompletableFuture
                    .<Integer>supplyAsync(() -> { throw new RuntimeException("boom"); }, pool)
                    .exceptionally(ex -> -1);

            CompletableFuture<Void> all = CompletableFuture.allOf(labelled, failing);
            all.get(2, TimeUnit.SECONDS);

            return Map.of(
                    "primitive", "CompletableFuture",
                    "labelled", labelled.get(),
                    "recovered", failing.get()
            );
        } finally {
            pool.shutdown();
        }
    }

    private int slow(int x) {
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return x * x;
    }
}
