package com.learning.orderprocessor.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

// Demonstrates: Dead Letter Topic consumer — observes records the main consumer gave up on
@Component
public class DltConsumer {

    private static final Logger log = LoggerFactory.getLogger(DltConsumer.class);

    @KafkaListener(
            topics = "${app.kafka-topics.orders-created}.DLT",
            groupId = "dlt-monitor",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDlt(ConsumerRecord<String, Object> record,
                      @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exMessage,
                      Acknowledgment ack) {
        log.error("DLT record key={} value={} cause={}", record.key(), record.value(), exMessage);
        ack.acknowledge();
    }
}
