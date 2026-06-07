package com.learning.orderprocessor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Demonstrates: @SpringBootTest + MockMvc to exercise a public concurrency demo endpoint
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        "orders.created", "orders.enriched", "orders.notifications", "orders.created.DLT"
})
class DemoControllerTest {

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(ctx).apply(springSecurity()).build();
    }

    @Test
    void atomicsDemoReturnsExpectedCounters() throws Exception {
        mvc().perform(get("/api/demo/atomics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atomicCounter").value(1000))
                .andExpect(jsonPath("$.longAdder").value(1000));
    }

    @Test
    void completableFutureDemoComposesValues() throws Exception {
        mvc().perform(get("/api/demo/completable-future"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelled").value("sum=13"))
                .andExpect(jsonPath("$.recovered").value(-1));
    }
}
