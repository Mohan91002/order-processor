package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

// Demonstrates: ForkJoinPool + RecursiveTask (divide and conquer)
@Component
public class ForkJoinDemo {

    public Map<String, Object> run() {
        long n = 200_000L;
        long sum = ForkJoinPool.commonPool().invoke(new SumTo(1, n));
        return Map.of("primitive", "ForkJoinPool + RecursiveTask", "sum1ToN", sum, "n", n);
    }

    private static final class SumTo extends RecursiveTask<Long> {
        private static final long THRESHOLD = 10_000;
        private final long from, to;
        SumTo(long from, long to) { this.from = from; this.to = to; }

        @Override
        protected Long compute() {
            long size = to - from;
            if (size <= THRESHOLD) {
                long s = 0L;
                for (long i = from; i <= to; i++) s += i;
                return s;
            }
            long mid = from + size / 2;
            SumTo left = new SumTo(from, mid);
            SumTo right = new SumTo(mid + 1, to);
            left.fork();
            long r = right.compute();
            long l = left.join();
            return l + r;
        }
    }
}
