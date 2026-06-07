package com.learning.orderprocessor.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

// Demonstrates: AtomicLong (compareAndSet hot path) and LongAdder (better under high contention).
@Service
public class StatsService {

    private final AtomicLong orderCount = new AtomicLong();
    private final LongAdder revenueCents = new LongAdder();
    private final LongAdder failures = new LongAdder();

    public void recordOrderCreated(long totalCents) {
        orderCount.incrementAndGet();
        revenueCents.add(totalCents);
    }

    public void recordFailure() {
        failures.increment();
    }

    public long orders() {
        return orderCount.get();
    }

    public long revenue() {
        return revenueCents.sum();
    }

    public long failureCount() {
        return failures.sum();
    }
}
