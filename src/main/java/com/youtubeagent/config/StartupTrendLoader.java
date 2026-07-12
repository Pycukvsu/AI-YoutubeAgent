package com.youtubeagent.config;

import com.youtubeagent.service.TrendDiscoveryService;
import com.youtubeagent.service.TrendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupTrendLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupTrendLoader.class);

    private final TrendDiscoveryService trendDiscoveryService;
    private final TrendService trendService;

    public StartupTrendLoader(TrendDiscoveryService trendDiscoveryService, TrendService trendService) {
        this.trendDiscoveryService = trendDiscoveryService;
        this.trendService = trendService;
    }

    @Override
    public void run(ApplicationArguments args) {
        long available = trendService.getAvailableTrendsCount();
        log.info("Available trends on startup: {}", available);

        if (available < 3) {
            log.info("Less than 3 trends available, running discovery...");
            try {
                int discovered = trendDiscoveryService.discoverTrends();
                log.info("Discovered {} new trends on startup", discovered);
            } catch (Exception e) {
                log.warn("Trend discovery on startup failed: {}", e.getMessage());
            }
        }
    }
}
