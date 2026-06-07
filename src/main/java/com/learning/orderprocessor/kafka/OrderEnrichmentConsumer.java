package com.learning.orderprocessor.kafka;

import com.learning.orderprocessor.domain.OrderStatus;
import com.learning.orderprocessor.kafka.events.OrderCreatedEvent;
import com.learning.orderprocessor.kafka.events.OrderEnrichedEvent;
import com.learning.orderprocessor.service.EnrichmentService;
import com.learning.orderprocessor.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

// Demonstrates: a Kafka consumer that itself runs a CompletableFuture pipeline before ack'ing.
@Component
public class OrderEnrichmentConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEnrichmentConsumer.class);

    private final EnrichmentService enrichment;
    private final OrderEventProducer producer;
    private final OrderService orders;

    public OrderEnrichmentConsumer(EnrichmentService enrichment, OrderEventProducer producer, OrderService orders) {
        this.enrichment = enrichment;
        this.producer = producer;
        this.orders = orders;
    }

    @KafkaListener(
            topics = "${app.kafka-topics.orders-created}",
            groupId = "enrichment-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(OrderCreatedEvent event, Acknowledgment ack) {
        log.info("enrichment-consumer received order {}", event.orderId());
        OrderCreatedEvent.Line first = event.items().get(0);
        CompletableFuture<EnrichmentService.EnrichmentResult> future =
                enrichment.enrich(event.orderId(), first.productId(), first.quantity());
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Enrichment failed for order {}", event.orderId(), ex);
                orders.markStatus(event.orderId(), OrderStatus.FAILED);
            } else {
                orders.markStatus(event.orderId(), OrderStatus.ENRICHED);
                producer.publishEnriched(new OrderEnrichedEvent(
                        event.orderId(), result.shippingEta(), result.priceTotal(), result.tag()));
            }
            ack.acknowledge();
        });
    }
}
