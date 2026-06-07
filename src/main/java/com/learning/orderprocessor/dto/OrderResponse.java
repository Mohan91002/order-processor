package com.learning.orderprocessor.dto;

import com.learning.orderprocessor.domain.Order;
import com.learning.orderprocessor.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerEmail,
        OrderStatus status,
        long totalCents,
        Instant createdAt,
        Instant enrichedAt,
        List<Line> items
) {
    public record Line(Long productId, int quantity, long unitPriceCents) {}

    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getCustomerEmail(),
                o.getStatus(),
                o.getTotalCents(),
                o.getCreatedAt(),
                o.getEnrichedAt(),
                o.getItems().stream()
                        .map(it -> new Line(it.getProductId(), it.getQuantity(), it.getUnitPriceCents()))
                        .toList()
        );
    }
}
