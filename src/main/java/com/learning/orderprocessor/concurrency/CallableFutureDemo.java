package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

// Demonstrates: Callable returning a value + Future.get with timeout
@Component
public class CallableFutureDemo {

    public Map<String, Object> run() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Callable<Integer> work = () -> {
                Thread.sleep(120);
                return 42;
            };
            Future<Integer> f = pool.submit(work);
            Integer value = f.get(500, TimeUnit.MILLISECONDS);
            return Map.of("primitive", "Callable + Future", "value", value, "done", f.isDone());
        } finally {
            pool.shutdownNow();
        }
    }
}
