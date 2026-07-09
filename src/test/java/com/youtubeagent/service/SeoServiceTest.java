package com.youtubeagent.service;

import com.youtubeagent.config.OpenAiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeoServiceTest {

    private SeoService seoService;

    @BeforeEach
    void setUp() {
        OpenAiConfig config = new OpenAiConfig();
        config.setApiKey("test-key");
        config.setModel("gpt-4o");
        config.setBaseUrl("https://api.openai.com/v1");
        config.setMaxTokens(800);

        seoService = new SeoService(config, new ObjectMapper());
    }

    @Test
    void optimizeThrowsOnInvalidApiKey() {
        assertThrows(Exception.class, () -> seoService.optimize("AI новости", "ru"));
    }

    @Test
    void suggestTitlesThrowsOnInvalidApiKey() {
        assertThrows(Exception.class, () -> seoService.suggestTitles("AI новости", 5));
    }

    @Test
    void seoResultDefaultValues() {
        SeoService.SeoResult result = new SeoService.SeoResult();

        assertNull(result.getTitle());
        assertNull(result.getDescription());
        assertNotNull(result.getTags());
        assertTrue(result.getTags().isEmpty());
        assertNotNull(result.getHashtags());
        assertTrue(result.getHashtags().isEmpty());
        assertNotNull(result.getKeywords());
        assertTrue(result.getKeywords().isEmpty());
        assertNotNull(result.getTips());
        assertTrue(result.getTips().isEmpty());
        assertEquals(0, result.getSeoScore());
    }
}
