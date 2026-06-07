package com.learning.orderprocessor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

// Demonstrates: @EnableRetry — turns on @Retryable / @Recover (used by ShippingClient)
@Configuration
@EnableRetry
public class RetryConfig {
}
