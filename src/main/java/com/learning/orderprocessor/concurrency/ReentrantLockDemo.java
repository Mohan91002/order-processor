package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Demonstrates: ReentrantLock with tryLock + Condition (await/signal)
@Component
public class ReentrantLockDemo {

    public Map<String, Object> run() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition ready = lock.newCondition();
        boolean[] flag = {false};
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            pool.submit(() -> {
                lock.lock();
                try {
                    Thread.sleep(100);
                    flag[0] = true;
                    ready.signalAll();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
                return null;
            });

            boolean acquired = lock.tryLock(2, TimeUnit.SECONDS);
            String tryLockResult = "acquired=" + acquired;
            try {
                while (!flag[0]) ready.await(1, TimeUnit.SECONDS);
            } finally {
                if (acquired) lock.unlock();
            }
            return Map.of("primitive", "ReentrantLock + Condition", "tryLock", tryLockResult, "flag", flag[0]);
        } finally {
            pool.shutdownNow();
        }
    }
}
