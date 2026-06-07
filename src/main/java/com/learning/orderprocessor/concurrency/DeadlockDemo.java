package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

// Demonstrates:
// 1) A classic two-lock deadlock recipe (NOT triggered — only sets it up if forceDeadlock=true)
// 2) The fix: always acquire locks in a globally consistent order (e.g. by lock identity hash)
@Component
public class DeadlockDemo {

    public Map<String, Object> run(boolean forceDeadlock) throws Exception {
        ReentrantLock a = new ReentrantLock();
        ReentrantLock b = new ReentrantLock();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            if (forceDeadlock) {
                pool.submit(() -> acquireBoth(a, b));
                pool.submit(() -> acquireBoth(b, a));
            } else {
                pool.submit(() -> acquireOrdered(a, b));
                pool.submit(() -> acquireOrdered(b, a));
            }
            pool.shutdown();
            boolean finished = pool.awaitTermination(2, TimeUnit.SECONDS);
            return Map.of(
                    "primitive", "Deadlock (set up + ordered-lock fix)",
                    "forceDeadlock", forceDeadlock,
                    "finishedCleanly", finished
            );
        } finally {
            pool.shutdownNow();
        }
    }

    private void acquireBoth(ReentrantLock first, ReentrantLock second) {
        first.lock();
        try {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            second.lock();
            try { /* work */ } finally { second.unlock(); }
        } finally {
            first.unlock();
        }
    }

    private void acquireOrdered(ReentrantLock x, ReentrantLock y) {
        ReentrantLock first = System.identityHashCode(x) < System.identityHashCode(y) ? x : y;
        ReentrantLock second = first == x ? y : x;
        acquireBoth(first, second);
    }
}
