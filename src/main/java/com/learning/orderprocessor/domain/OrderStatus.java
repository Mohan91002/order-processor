package com.learning.orderprocessor.domain;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    ENRICHED,
    CONFIRMED,
    FAILED,
    CANCELLED
}
