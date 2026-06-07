package com.learning.orderprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Demonstrates: @ConfigurationProperties — strongly-typed binding of "app.*" from application.yml
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Executor executor,
        KafkaTopics kafkaTopics,
        External external
) {
    public record Jwt(String secret, long accessTokenTtlMinutes) {}
    public record Executor(int corePoolSize, int maxPoolSize, int queueCapacity, String threadNamePrefix) {}
    public record KafkaTopics(String ordersCreated, String ordersEnriched, String notifications) {}
    public record External(String shippingBaseUrl) {}
}
