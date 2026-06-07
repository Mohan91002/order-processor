package com.learning.orderprocessor.service;

import com.learning.orderprocessor.client.ShippingClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// Demonstrates:
// - @Async returning CompletableFuture (Spring Async)
// - CompletableFuture.supplyAsync / thenCombine / allOf composition
// - Semaphore bounding concurrent external calls
@Service
public class EnrichmentService {

    private final ShippingClient shipping;
    private final PricingService pricing;
    private final Executor executor;
    private final Semaphore externalGate = new Semaphore(10);

    public EnrichmentService(ShippingClient shipping,
                             PricingService pricing,
                             @Qualifier("appTaskExecutor") Executor executor) {
        this.shipping = shipping;
        this.pricing = pricing;
        this.executor = executor;
    }

    public record EnrichmentResult(String shippingEta, long priceTotal, String tag) {}

    @Async("appTaskExecutor")
    public CompletableFuture<EnrichmentResult> enrich(Long orderId, Long productId, int quantity) {
        CompletableFuture<String> etaF = CompletableFuture.supplyAsync(() -> withGate(() -> shipping.fetchEta(orderId)), executor);
        CompletableFuture<Long> priceF = CompletableFuture.supplyAsync(() -> pricing.getPriceCents(productId) * quantity, executor);
        CompletableFuture<String> tagF = CompletableFuture.supplyAsync(() -> "ENRICHED-" + Thread.currentThread().getName(), executor);

        return CompletableFuture.allOf(etaF, priceF, tagF)
                .thenApplyAsync(v -> new EnrichmentResult(etaF.join(), priceF.join(), tagF.join()), executor)
                .exceptionally(ex -> new EnrichmentResult("unknown", 0L, "FAILED: " + ex.getMessage()));
    }

    private <T> T withGate(java.util.function.Supplier<T> s) {
        try {
            if (!externalGate.tryAcquire(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("external call gate timed out");
            }
            try {
                return s.get();
            } finally {
                externalGate.release();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        }
    }
}
