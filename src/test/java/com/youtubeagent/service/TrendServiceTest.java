package com.youtubeagent.service;

import com.youtubeagent.entity.Trend;
import com.youtubeagent.repository.TrendRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendServiceTest {

    @Mock
    private TrendRepository trendRepository;

    private TrendService trendService;

    @BeforeEach
    void setUp() {
        trendService = new TrendService(trendRepository);
    }

    @Test
    void addTrendSavesAndReturns() {
        when(trendRepository.save(any())).thenAnswer(inv -> {
            Trend t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trend trend = trendService.addTrend("AI news", "manual");

        assertEquals("AI news", trend.getTopic());
        assertEquals("manual", trend.getSource());
        assertNotNull(trend.getDiscoveredAt());
        verify(trendRepository).save(any(Trend.class));
    }

    @Test
    void getAvailableTrendsDelegatesToRepo() {
        Trend t1 = new Trend("Topic 1", "manual");
        Trend t2 = new Trend("Topic 2", "manual");
        when(trendRepository.findByStatusOrderByDiscoveredAtDesc("new")).thenReturn(List.of(t1, t2));

        List<Trend> trends = trendService.getAvailableTrends();

        assertEquals(2, trends.size());
        verify(trendRepository).findByStatusOrderByDiscoveredAtDesc("new");
    }

    @Test
    void getBestTrendReturnsTopByVolume() {
        Trend best = new Trend("Hot topic", "youtube");
        best.setSearchVolume(10000);
        when(trendRepository.findFirstByStatusOrderBySearchVolumeDesc("new")).thenReturn(Optional.of(best));

        Trend result = trendService.getBestTrend();

        assertNotNull(result);
        assertEquals("Hot topic", result.getTopic());
    }

    @Test
    void getBestTrendReturnsNullWhenEmpty() {
        when(trendRepository.findFirstByStatusOrderBySearchVolumeDesc("new")).thenReturn(Optional.empty());

        assertNull(trendService.getBestTrend());
    }

    @Test
    void getAvailableTrendsCountDelegatesToRepo() {
        when(trendRepository.countByStatus("new")).thenReturn(5L);

        assertEquals(5, trendService.getAvailableTrendsCount());
    }

    @Test
    void expireOldTrendsMarksExpired() {
        Trend oldTrend = new Trend("Old topic", "manual");
        oldTrend.setDiscoveredAt(LocalDateTime.now().minusDays(10));

        Trend freshTrend = new Trend("Fresh topic", "manual");
        freshTrend.setDiscoveredAt(LocalDateTime.now().minusDays(2));

        when(trendRepository.findByStatusOrderByDiscoveredAtDesc("new")).thenReturn(List.of(oldTrend, freshTrend));
        when(trendRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        trendService.expireOldTrends();

        assertEquals("expired", oldTrend.getStatus());
        assertEquals("new", freshTrend.getStatus());
        verify(trendRepository, times(1)).save(oldTrend);
    }
}
