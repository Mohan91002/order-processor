package com.learning.orderprocessor.controller;

import com.learning.orderprocessor.service.ReportingService;
import com.learning.orderprocessor.service.StatsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService stats;
    private final ReportingService reporting;

    public StatsController(StatsService stats, ReportingService reporting) {
        this.stats = stats;
        this.reporting = reporting;
    }

    @GetMapping("/counters")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Map<String, Object> counters() {
        return Map.of(
                "ordersCreated", stats.orders(),
                "revenueCents", stats.revenue(),
                "failures", stats.failureCount()
        );
    }

    @GetMapping("/by-status-24h")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Map<String, Long> byStatus24h() {
        return reporting.totalsByStatusLast24h();
    }
}
