package com.learning.orderprocessor.scheduled;

import com.learning.orderprocessor.service.ReportingService;
import com.learning.orderprocessor.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Demonstrates: @Scheduled with cron (every 30s here so it's visible during a demo)
@Component
public class DailyStatsJob {

    private static final Logger log = LoggerFactory.getLogger(DailyStatsJob.class);
    private final ReportingService reporting;
    private final StatsService stats;

    public DailyStatsJob(ReportingService reporting, StatsService stats) {
        this.reporting = reporting;
        this.stats = stats;
    }

    @Scheduled(cron = "0/30 * * * * *")
    public void aggregate() {
        log.info("[scheduled cron] orders={}, revenueCents={}, failures={}, byStatus24h={}",
                stats.orders(), stats.revenue(), stats.failureCount(), reporting.totalsByStatusLast24h());
    }
}
