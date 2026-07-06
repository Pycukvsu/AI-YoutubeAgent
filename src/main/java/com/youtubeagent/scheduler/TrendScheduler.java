package com.youtubeagent.scheduler;

import com.youtubeagent.service.TrendDiscoveryService;
import com.youtubeagent.service.TrendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrendScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrendScheduler.class);

    private final TrendDiscoveryService trendDiscoveryService;
    private final TrendService trendService;

    public TrendScheduler(TrendDiscoveryService trendDiscoveryService, TrendService trendService) {
        this.trendDiscoveryService = trendDiscoveryService;
        this.trendService = trendService;
    }

    @Scheduled(cron = "${scheduling.trend-cron:0 6 * * *}")
    public void discoverTrends() {
        log.info("Scheduled trend discovery started");
        int newTrends = trendDiscoveryService.discoverTrends();
        trendService.expireOldTrends();
        long available = trendService.getAvailableTrendsCount();
        log.info("Trend discovery completed: {} new trends, {} available total", newTrends, available);
    }
}
