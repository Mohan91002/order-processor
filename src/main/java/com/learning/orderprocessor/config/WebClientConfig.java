package com.learning.orderprocessor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// Demonstrates: WebClient bean wired from @ConfigurationProperties (used in ShippingClient)
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient shippingWebClient(AppProperties props) {
        return WebClient.builder()
                .baseUrl(props.external().shippingBaseUrl())
                .build();
    }
}
