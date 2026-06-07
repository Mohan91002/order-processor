package com.learning.orderprocessor.kafka.events;

import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        String customerEmail,
        long totalCents,
        List<Line> items,
        Instant createdAt
) {
    public record Line(Long productId, int quantity, long unitPriceCents) {}
}
