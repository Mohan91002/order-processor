package com.learning.orderprocessor.kafka.events;

public record OrderEnrichedEvent(
        Long orderId,
        String shippingEta,
        long enrichedTotalCents,
        String tag
) {
}
