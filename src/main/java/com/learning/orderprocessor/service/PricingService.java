package com.learning.orderprocessor.service;

import com.learning.orderprocessor.repo.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

// Demonstrates:
// - StampedLock optimistic read (lock-free fast path) + write lock for updates
// - @Cacheable / @CacheEvict layered on top
@Service
public class PricingService {

    private final ProductRepository products;
    private final StampedLock lock = new StampedLock();
    private final Map<Long, Long> priceCache = new HashMap<>();

    public PricingService(ProductRepository products) {
        this.products = products;
    }

    @Cacheable(cacheNames = "productPrices", key = "#productId")
    public long getPriceCents(Long productId) {
        long stamp = lock.tryOptimisticRead();
        Long cached = priceCache.get(productId);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                cached = priceCache.get(productId);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        if (cached != null) return cached;

        long fresh = products.findById(productId).map(p -> p.getPriceCents()).orElse(0L);
        long writeStamp = lock.writeLock();
        try {
            priceCache.put(productId, fresh);
        } finally {
            lock.unlockWrite(writeStamp);
        }
        return fresh;
    }

    @CacheEvict(cacheNames = "productPrices", key = "#productId")
    public void invalidate(Long productId) {
        long writeStamp = lock.writeLock();
        try {
            priceCache.remove(productId);
        } finally {
            lock.unlockWrite(writeStamp);
        }
    }
}
