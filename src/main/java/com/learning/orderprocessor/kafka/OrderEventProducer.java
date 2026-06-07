package com.learning.orderprocessor.kafka;

import com.learning.orderprocessor.config.AppProperties;
import com.learning.orderprocessor.kafka.events.OrderCreatedEvent;
import com.learning.orderprocessor.kafka.events.OrderEnrichedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

// Demonstrates: KafkaTemplate.send returning a CompletableFuture<SendResult> (Spring Kafka 3.x)
@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> template;
    private final AppProperties props;

    public OrderEventProducer(KafkaTemplate<String, Object> template, AppProperties props) {
        this.template = template;
        this.props = props;
    }

    public void publishCreated(OrderCreatedEvent event) {
        String key = String.valueOf(event.orderId());
        CompletableFuture<SendResult<String, Object>> future =
                template.send(props.kafkaTopics().ordersCreated(), key, event);
        future.whenComplete((res, ex) -> {
            if (ex != null) log.error("Failed to publish orders.created for {}: {}", event.orderId(), ex.toString());
            else log.info("Published orders.created [{}] to {}-{}@{}", event.orderId(),
                    res.getRecordMetadata().topic(), res.getRecordMetadata().partition(), res.getRecordMetadata().offset());
        });
    }

    public void publishEnriched(OrderEnrichedEvent event) {
        template.send(props.kafkaTopics().ordersEnriched(), String.valueOf(event.orderId()), event);
    }

    public void publishNotification(String body) {
        template.send(props.kafkaTopics().notifications(), body);
    }
}
