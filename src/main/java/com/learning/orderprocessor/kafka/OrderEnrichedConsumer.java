package com.learning.orderprocessor.kafka;

import com.learning.orderprocessor.domain.OrderStatus;
import com.learning.orderprocessor.kafka.events.OrderEnrichedEvent;
import com.learning.orderprocessor.service.NotificationService;
import com.learning.orderprocessor.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

// Demonstrates: consumer that drives the local NotificationService BlockingQueue + marks final status
@Component
public class OrderEnrichedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEnrichedConsumer.class);

    private final NotificationService notifications;
    private final OrderService orders;

    public OrderEnrichedConsumer(NotificationService notifications, OrderService orders) {
        this.notifications = notifications;
        this.orders = orders;
    }

    @KafkaListener(
            topics = "${app.kafka-topics.orders-enriched}",
            groupId = "confirmation-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(OrderEnrichedEvent event, Acknowledgment ack) {
        log.info("confirmation-consumer received enriched order {}", event.orderId());
        orders.markStatus(event.orderId(), OrderStatus.CONFIRMED);
        notifications.enqueue("Order " + event.orderId() + " confirmed. ETA: " + event.shippingEta());
        ack.acknowledge();
    }
}
