package com.learning.orderprocessor.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

// Demonstrates:
// - WebClient (reactive HTTP client used synchronously via .block)
// - @Retryable with @Recover fallback (Spring Retry)
@Component
public class ShippingClient {

    private static final Logger log = LoggerFactory.getLogger(ShippingClient.class);
    private final WebClient client;

    public ShippingClient(WebClient shippingWebClient) {
        this.client = shippingWebClient;
    }

    @Retryable(
            retryFor = {WebClientResponseException.class, java.net.ConnectException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public String fetchEta(Long orderId) {
        log.debug("calling shipping for order {}", orderId);
        return client.get()
                .uri("/uuid")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(2))
                .block();
    }

    @Recover
    public String recoverEta(Exception ex, Long orderId) {
        log.warn("shipping call exhausted retries for {}: {}", orderId, ex.toString());
        return "unknown-eta";
    }
}
