package com.learning.orderprocessor.actuator;

import com.learning.orderprocessor.service.StatsService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

// Demonstrates: custom HealthIndicator surfacing app metrics under /actuator/health
@Component("orders")
public class OrdersHealthIndicator implements HealthIndicator {

    private final StatsService stats;

    public OrdersHealthIndicator(StatsService stats) {
        this.stats = stats;
    }

    @Override
    public Health health() {
        long failures = stats.failureCount();
        Health.Builder builder = failures > 100 ? Health.down() : Health.up();
        return builder
                .withDetail("ordersCreated", stats.orders())
                .withDetail("revenueCents", stats.revenue())
                .withDetail("failures", failures)
                .build();
    }
}
