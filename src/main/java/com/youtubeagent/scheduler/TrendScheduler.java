package com.youtubeagent.scheduler;

import com.youtubeagent.service.AnalyticsService;
import com.youtubeagent.service.TrendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrendScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrendScheduler.class);

    private final TrendService trendService;

    public TrendScheduler(TrendService trendService) {
        this.trendService = trendService;
    }

    @Scheduled(cron = "${scheduling.trend-cron:0 6 * * *}")
    public void discoverTrends() {
        log.info("Scheduled trend discovery started");
        trendService.expireOldTrends();
        log.info("Trend discovery completed, {} available trends", trendService.getAvailableTrendsCount());
    }
}
