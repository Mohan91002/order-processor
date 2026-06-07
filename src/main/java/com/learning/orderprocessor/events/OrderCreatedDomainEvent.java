package com.learning.orderprocessor.events;

// Spring application event published by OrderService after a successful save.
public record OrderCreatedDomainEvent(Long orderId, String customerEmail, long totalCents) {
}
