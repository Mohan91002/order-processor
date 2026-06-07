package com.learning.orderprocessor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

// Demonstrates: @EnableScheduling + a tuned TaskScheduler (multiple worker threads for @Scheduled jobs)
@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler sch = new ThreadPoolTaskScheduler();
        sch.setPoolSize(4);
        sch.setThreadNamePrefix("scheduler-");
        sch.setWaitForTasksToCompleteOnShutdown(true);
        return sch;
    }
}
