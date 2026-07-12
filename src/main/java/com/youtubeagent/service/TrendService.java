package com.youtubeagent.service;

import com.youtubeagent.entity.Trend;
import com.youtubeagent.repository.TrendRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrendService {

    private static final Logger log = LoggerFactory.getLogger(TrendService.class);

    private final TrendRepository trendRepository;

    public TrendService(TrendRepository trendRepository) {
        this.trendRepository = trendRepository;
    }

    public Trend addTrend(String topic, String source) {
        Trend trend = new Trend(topic, source);
        trend.setDiscoveredAt(LocalDateTime.now());
        trend = trendRepository.save(trend);
        log.info("Added trend: '{}' (source: {})", topic, source);
        return trend;
    }

    public List<Trend> getAvailableTrends() {
        return trendRepository.findByStatusOrderByDiscoveredAtDesc("new");
    }

    public Trend getBestTrend() {
        return trendRepository.findFirstByStatusOrderBySearchVolumeDesc("new").orElse(null);
    }

    public long getAvailableTrendsCount() {
        return trendRepository.countByStatus("new");
    }

    public void expireOldTrends() {
        List<Trend> oldTrends = trendRepository.findByStatusOrderByDiscoveredAtDesc("new");
        for (Trend trend : oldTrends) {
            if (trend.getDiscoveredAt().plusDays(7).isBefore(LocalDateTime.now())) {
                trend.setStatus("expired");
                trendRepository.save(trend);
            }
        }
    }

    public void deleteTrend(Long id) {
        trendRepository.deleteById(id);
        log.info("Deleted trend with id: {}", id);
    }

    public Trend findOrCreateTrend(String topic, String source, String category) {
        return trendRepository.findByTopicIgnoreCaseAndStatus(topic, "new")
                .orElseGet(() -> {
                    Trend trend = new Trend(topic, source);
                    trend.setCategory(category);
                    trend.setDiscoveredAt(LocalDateTime.now());
                    return trendRepository.save(trend);
                });
    }
}
