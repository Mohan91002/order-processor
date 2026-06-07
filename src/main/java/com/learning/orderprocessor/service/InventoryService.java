package com.learning.orderprocessor.service;

import com.learning.orderprocessor.repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// Demonstrates: per-product ReentrantLock + ConcurrentHashMap for fine-grained in-memory locking
// (Layered ON TOP of DB-level pessimistic locking — illustrates both strategies.)
@Service
public class InventoryService {

    private final ProductRepository products;
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public InventoryService(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    public boolean reserve(Long productId, int quantity) {
        ReentrantLock lock = locks.computeIfAbsent(productId, k -> new ReentrantLock(true));
        lock.lock();
        try {
            // DB-side pessimistic decrement: returns rows affected
            int updated = products.decrementStock(productId, quantity);
            return updated > 0;
        } finally {
            lock.unlock();
        }
    }
}
