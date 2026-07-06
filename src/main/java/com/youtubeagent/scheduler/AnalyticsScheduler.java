package com.youtubeagent.scheduler;

import com.youtubeagent.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsScheduler.class);

    private final AnalyticsService analyticsService;

    public AnalyticsScheduler(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Scheduled(cron = "${scheduling.analytics-cron:0 2 * * *}")
    public void refreshAnalytics() {
        log.info("Scheduled analytics refresh started");
        analyticsService.refreshAllAnalytics();
        log.info("Analytics refresh completed");
    }
}
