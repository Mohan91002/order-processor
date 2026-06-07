package com.learning.orderprocessor.kafka;

import com.learning.orderprocessor.domain.OrderStatus;
import com.learning.orderprocessor.kafka.events.OrderCreatedEvent;
import com.learning.orderprocessor.service.InventoryService;
import com.learning.orderprocessor.service.OrderService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

// Demonstrates:
// - @KafkaListener with manual ack (MANUAL_IMMEDIATE)
// - Listening on the orders.created topic with its own consumer group
@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final InventoryService inventory;
    private final OrderService orders;

    public OrderCreatedConsumer(InventoryService inventory, OrderService orders) {
        this.inventory = inventory;
        this.orders = orders;
    }

    @KafkaListener(
            topics = "${app.kafka-topics.orders-created}",
            groupId = "inventory-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, OrderCreatedEvent> record, Acknowledgment ack) {
        OrderCreatedEvent event = record.value();
        log.info("inventory-consumer received order {} on {}-{}@{}", event.orderId(),
                record.topic(), record.partition(), record.offset());
        try {
            boolean allReserved = event.items().stream()
                    .allMatch(line -> inventory.reserve(line.productId(), line.quantity()));
            orders.markStatus(event.orderId(),
                    allReserved ? OrderStatus.INVENTORY_RESERVED : OrderStatus.FAILED);
            ack.acknowledge();
        } catch (RuntimeException re) {
            log.error("Inventory reservation failed for order {}", event.orderId(), re);
            throw re; // bubble up so DefaultErrorHandler can retry + route to DLT
        }
    }
}
