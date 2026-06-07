package com.learning.orderprocessor.service;

import com.learning.orderprocessor.domain.Order;
import com.learning.orderprocessor.repo.OrderRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

// Demonstrates:
// - ForkJoinPool with a custom RecursiveTask
// - Parallel streams scoped to a ForkJoinPool to avoid leaking work to the common pool
@Service
public class ReportingService {

    private final OrderRepository orders;
    private final ForkJoinPool pool = new ForkJoinPool(4);

    public ReportingService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "orderStats", key = "'24h'")
    public Map<String, Long> totalsByStatusLast24h() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        List<Order> recent = orders.findByCreatedAtAfter(since);
        // Run the parallel stream inside our dedicated FJP — keeps it off the common pool.
        return pool.submit(() ->
                recent.parallelStream()
                        .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()))
        ).join();
    }

    public long sumWithForkJoin(List<Long> values) {
        return pool.invoke(new SumTask(values, 0, values.size()));
    }

    private static final class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 1_000;
        private final List<Long> data;
        private final int from, to;

        SumTask(List<Long> data, int from, int to) {
            this.data = data; this.from = from; this.to = to;
        }

        @Override
        protected Long compute() {
            int size = to - from;
            if (size <= THRESHOLD) {
                long s = 0L;
                for (int i = from; i < to; i++) s += data.get(i);
                return s;
            }
            int mid = from + size / 2;
            SumTask left = new SumTask(data, from, mid);
            SumTask right = new SumTask(data, mid, to);
            left.fork();
            long r = right.compute();
            long l = left.join();
            return l + r;
        }
    }
}
