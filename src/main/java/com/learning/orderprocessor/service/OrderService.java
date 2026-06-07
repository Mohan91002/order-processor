package com.learning.orderprocessor.service;

import com.learning.orderprocessor.domain.Order;
import com.learning.orderprocessor.domain.OrderItem;
import com.learning.orderprocessor.domain.OrderStatus;
import com.learning.orderprocessor.domain.Product;
import com.learning.orderprocessor.dto.CreateOrderRequest;
import com.learning.orderprocessor.events.OrderCreatedDomainEvent;
import com.learning.orderprocessor.repo.OrderRepository;
import com.learning.orderprocessor.repo.ProductRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

// Demonstrates: @Transactional + ApplicationEventPublisher (Spring events) feeding the async pipeline.
@Service
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final ApplicationEventPublisher events;
    private final StatsService stats;

    public OrderService(OrderRepository orders,
                        ProductRepository products,
                        ApplicationEventPublisher events,
                        StatsService stats) {
        this.orders = orders;
        this.products = products;
        this.events = events;
        this.stats = stats;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        Order order = Order.builder()
                .customerEmail(req.customerEmail())
                .status(OrderStatus.PENDING)
                .build();

        long total = 0L;
        for (CreateOrderRequest.Item line : req.items()) {
            Product p = products.findById(line.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown product " + line.productId()));
            OrderItem item = OrderItem.builder()
                    .productId(p.getId())
                    .quantity(line.quantity())
                    .unitPriceCents(p.getPriceCents())
                    .build();
            order.addItem(item);
            total += p.getPriceCents() * line.quantity();
        }
        order.setTotalCents(total);

        Order saved = orders.save(order);
        stats.recordOrderCreated(total);
        events.publishEvent(new OrderCreatedDomainEvent(saved.getId(), saved.getCustomerEmail(), saved.getTotalCents()));
        return saved;
    }

    @Transactional
    public void markStatus(Long id, OrderStatus status) {
        orders.findById(id).ifPresent(o -> {
            o.setStatus(status);
            if (status == OrderStatus.ENRICHED) o.setEnrichedAt(Instant.now());
        });
    }

    @Transactional(readOnly = true)
    public Order getRequired(Long id) {
        return orders.findById(id).orElseThrow(() -> new IllegalArgumentException("Order " + id + " not found"));
    }
}
