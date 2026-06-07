package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Demonstrates: Classic producer/consumer with synchronized + wait()/notifyAll()
@Component
public class ProducerConsumerWaitNotifyDemo {

    private static final int CAPACITY = 3;

    public Map<String, Object> run() throws Exception {
        Buffer buf = new Buffer();
        AtomicInteger consumed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            pool.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    try { buf.put(i); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
            pool.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    try { buf.take(); consumed.incrementAndGet(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
            pool.shutdown();
            pool.awaitTermination(3, TimeUnit.SECONDS);
            return Map.of("primitive", "synchronized + wait/notifyAll", "consumed", consumed.get());
        } finally {
            pool.shutdownNow();
        }
    }

    static class Buffer {
        private final Deque<Integer> q = new ArrayDeque<>();

        synchronized void put(int v) throws InterruptedException {
            while (q.size() == CAPACITY) wait();
            q.add(v);
            notifyAll();
        }

        synchronized int take() throws InterruptedException {
            while (q.isEmpty()) wait();
            int v = q.removeFirst();
            notifyAll();
            return v;
        }
    }
}
