package com.learning.orderprocessor.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// Demonstrates:
// - In-process producer/consumer with BlockingQueue (LinkedBlockingQueue)
// - Dedicated ExecutorService (cached pool) as consumer workers
// - Graceful shutdown via @PreDestroy
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int WORKERS = 3;

    private final BlockingQueue<String> mailbox = new LinkedBlockingQueue<>(500);
    private final ExecutorService workers = Executors.newFixedThreadPool(WORKERS, r -> {
        Thread t = new Thread(r);
        t.setName("notif-worker-" + t.getId());
        t.setDaemon(true);
        return t;
    });
    private volatile boolean running = true;

    @PostConstruct
    void start() {
        for (int i = 0; i < WORKERS; i++) {
            workers.submit(this::loop);
        }
        log.info("NotificationService started with {} workers", WORKERS);
    }

    public void enqueue(String message) {
        boolean offered = mailbox.offer(message);
        if (!offered) log.warn("Notification mailbox full; dropping: {}", message);
    }

    private void loop() {
        while (running) {
            try {
                String msg = mailbox.poll(1, TimeUnit.SECONDS);
                if (msg != null) deliver(msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void deliver(String message) {
        log.info("[{}] delivering notification: {}", Thread.currentThread().getName(), message);
    }

    @PreDestroy
    void shutdown() throws InterruptedException {
        running = false;
        workers.shutdown();
        if (!workers.awaitTermination(5, TimeUnit.SECONDS)) workers.shutdownNow();
    }
}
