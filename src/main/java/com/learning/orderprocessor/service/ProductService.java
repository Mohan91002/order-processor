package com.learning.orderprocessor.service;

import com.learning.orderprocessor.domain.Product;
import com.learning.orderprocessor.repo.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Demonstrates: @Cacheable / @CacheEvict on a JPA-backed service.
@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    @Cacheable("products")
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return products.findAll();
    }

    @Cacheable(cacheNames = "products", key = "#id")
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return products.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public void evictAll() {}
}
