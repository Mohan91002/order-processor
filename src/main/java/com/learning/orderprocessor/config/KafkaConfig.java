package com.learning.orderprocessor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

// Demonstrates:
// - NewTopic beans (auto-created by Spring KafkaAdmin)
// - Custom ConsumerFactory + ConcurrentKafkaListenerContainerFactory
// - DefaultErrorHandler with FixedBackOff + DeadLetterPublishingRecoverer (DLT)
@Configuration
public class KafkaConfig {

    private final AppProperties props;
    private final KafkaProperties kafkaProperties;

    public KafkaConfig(AppProperties props, KafkaProperties kafkaProperties) {
        this.props = props;
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public NewTopic ordersCreatedTopic() {
        return TopicBuilder.name(props.kafkaTopics().ordersCreated()).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic ordersEnrichedTopic() {
        return TopicBuilder.name(props.kafkaTopics().ordersEnriched()).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(props.kafkaTopics().notifications()).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic ordersCreatedDlt() {
        return TopicBuilder.name(props.kafkaTopics().ordersCreated() + ".DLT").partitions(1).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(null));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            @Qualifier("kafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        DeadLetterPublishingRecoverer dlt = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + ".DLT", record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(dlt, new FixedBackOff(1000L, 2));
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
