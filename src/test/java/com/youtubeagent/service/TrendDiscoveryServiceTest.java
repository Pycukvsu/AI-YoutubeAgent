package com.youtubeagent.service;

import com.youtubeagent.config.TrendDiscoveryConfig;
import com.youtubeagent.entity.Trend;
import com.youtubeagent.repository.TrendRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendDiscoveryServiceTest {

    @Mock
    private OpenAiService openAiService;
    @Mock
    private YoutubeUploadService youtubeUploadService;
    @Mock
    private GoogleTrendsService googleTrendsService;
    @Mock
    private TrendRepository trendRepository;

    private TrendDiscoveryService trendDiscoveryService;
    private TrendDiscoveryConfig config;

    @BeforeEach
    void setUp() {
        config = new TrendDiscoveryConfig();
        config.setTopicsPerRun(5);
        config.setLanguage("ru");
        config.setMinViralScore(7);
        config.setEnableOpenAiGeneration(false);
        config.setEnableYoutubeSearch(false);
        config.setEnableGoogleTrends(false);

        trendDiscoveryService = new TrendDiscoveryService(
                config, openAiService, youtubeUploadService, googleTrendsService,
                trendRepository, new ObjectMapper());
    }

    @Test
    void discoverTrendsReturnsZeroWhenDisabled() {
        int result = trendDiscoveryService.discoverTrends();
        assertEquals(0, result);
    }

    @Test
    void findOrCreateTrendCreatesNewTrend() {
        when(trendRepository.findByTopicIgnoreCaseAndStatus("AI новости", "new"))
                .thenReturn(Optional.empty());
        when(trendRepository.save(any())).thenAnswer(inv -> {
            Trend t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        boolean created = trendDiscoveryService.findOrCreateTrend("AI новости", "openai", "technology");

        assertTrue(created);
        verify(trendRepository).save(any(Trend.class));
    }

    @Test
    void findOrCreateTrendReturnsFalseWhenExists() {
        Trend existing = new Trend("AI новости", "openai");
        existing.setId(1L);
        when(trendRepository.findByTopicIgnoreCaseAndStatus("AI новости", "new"))
                .thenReturn(Optional.of(existing));

        boolean created = trendDiscoveryService.findOrCreateTrend("AI новости", "openai", "technology");

        assertFalse(created);
        verify(trendRepository, never()).save(any());
    }

    @Test
    void findOrCreateTrendIsCaseInsensitive() {
        when(trendRepository.findByTopicIgnoreCaseAndStatus("AI НОВОСТИ", "new"))
                .thenReturn(Optional.empty());
        when(trendRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean created = trendDiscoveryService.findOrCreateTrend("AI НОВОСТИ", "openai", "technology");

        assertTrue(created);
    }
}
