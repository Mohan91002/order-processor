package com.learning.orderprocessor.events;

import com.learning.orderprocessor.domain.Order;
import com.learning.orderprocessor.kafka.OrderEventProducer;
import com.learning.orderprocessor.kafka.events.OrderCreatedEvent;
import com.learning.orderprocessor.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Demonstrates:
// - @TransactionalEventListener (AFTER_COMMIT) — only fire after DB transaction succeeds
// - Bridges in-process Spring application event → Kafka topic
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final OrderService orders;
    private final OrderEventProducer producer;

    public OrderEventListener(OrderService orders, OrderEventProducer producer) {
        this.orders = orders;
        this.producer = producer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedDomainEvent event) {
        log.info("AFTER_COMMIT bridge: forwarding order {} to Kafka", event.orderId());
        Order o = orders.getRequired(event.orderId());
        var lines = o.getItems().stream()
                .map(i -> new OrderCreatedEvent.Line(i.getProductId(), i.getQuantity(), i.getUnitPriceCents()))
                .toList();
        producer.publishCreated(new OrderCreatedEvent(
                o.getId(),
                o.getCustomerEmail(),
                o.getTotalCents(),
                lines,
                o.getCreatedAt()
        ));
    }
}
