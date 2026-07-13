package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.entity.MiniSeries;
import com.youtubeagent.exception.ExternalServiceException;
import com.youtubeagent.repository.MiniSeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MiniSeriesService {

    private static final Logger log = LoggerFactory.getLogger(MiniSeriesService.class);

    private final OpenAiService openAiService;
    private final MiniSeriesRepository miniSeriesRepository;
    private final ObjectMapper objectMapper;

    public MiniSeriesService(OpenAiService openAiService,
                              MiniSeriesRepository miniSeriesRepository,
                              ObjectMapper objectMapper) {
        this.openAiService = openAiService;
        this.miniSeriesRepository = miniSeriesRepository;
        this.objectMapper = objectMapper;
    }

    public MiniSeries createSeries(String topic, int episodeCount) {
        try {
            String prompt = """
                    Создай концепцию мини-сериала из %d серий для YouTube Shorts на тему: "%s"
                    Каждая серия — 30-60 секунд, с сюжетом и интригой.

                    Формат — строго JSON:
                    {
                        "title": "название сериала",
                        "concept": "общая идея сериала (2 предложения)",
                        "episodes": [
                            {
                                "episode_number": 1,
                                "title": "название серии",
                                "hook": "цепляющая первая фраза",
                                "scene_description": "описание визуальной сцены на английском для генерации изображения",
                                "script_text": "текст сценария для озвучки на русском (30-60 сек)",
                                "cliffhanger": "интрига в конце"
                            }
                        ]
                    }

                    Правила:
                    - Каждая серия должна заканчиваться интригой (cliffhanger)
                    - Последняя серия должна раскрывать тайну
                    - Сцены должны быть ВИЗУАЛЬНО интересными
                    - Сценарии на русском языке
                    - Scene_description на АНГЛИЙСКОМ для генерации изображений
                    """.formatted(episodeCount, topic);

            Map<String, Object> response = openAiService.callOpenAi(prompt, 2000);
            String content = (String) response.get("content");
            JsonNode seriesJson = objectMapper.readTree(content);

            MiniSeries series = new MiniSeries();
            series.setTitle(seriesJson.path("title").asText());
            series.setConcept(seriesJson.path("concept").asText());
            series.setTotalEpisodes(episodeCount);
            series.setCurrentEpisode(0);
            series.setStatus("created");
            series.setEpisodesJson(content);
            series.setLanguage("ru");

            series = miniSeriesRepository.save(series);
            log.info("Created mini-series: '{}' with {} episodes", series.getTitle(), episodeCount);
            return series;

        } catch (Exception e) {
            log.error("Failed to create mini-series: {}", e.getMessage());
            throw new ExternalServiceException("MiniSeries", e);
        }
    }

    public Map<String, Object> getNextEpisode(Long seriesId) {
        try {
            MiniSeries series = miniSeriesRepository.findById(seriesId)
                    .orElseThrow(() -> new ExternalServiceException("MiniSeries", "Series not found"));

            if (series.getCurrentEpisode() >= series.getTotalEpisodes()) {
                series.setStatus("completed");
                series.setCompletedAt(LocalDateTime.now());
                miniSeriesRepository.save(series);
                return null;
            }

            JsonNode episodes = objectMapper.readTree(series.getEpisodesJson()).path("episodes");
            int epIndex = series.getCurrentEpisode();

            if (epIndex >= episodes.size()) {
                return null;
            }

            JsonNode episode = episodes.get(epIndex);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("seriesTitle", series.getTitle());
            result.put("episodeNumber", episode.path("episode_number").asInt());
            result.put("episodeTitle", episode.path("title").asText());
            result.put("hook", episode.path("hook").asText());
            result.put("sceneDescription", episode.path("scene_description").asText());
            result.put("scriptText", episode.path("script_text").asText());
            result.put("cliffhanger", episode.path("cliffhanger").asText());
            result.put("totalEpisodes", series.getTotalEpisodes());

            series.setCurrentEpisode(epIndex + 1);
            miniSeriesRepository.save(series);

            return result;

        } catch (Exception e) {
            log.error("Failed to get next episode: {}", e.getMessage());
            throw new ExternalServiceException("MiniSeries", e);
        }
    }

    public List<MiniSeries> getAllSeries() {
        return miniSeriesRepository.findAll();
    }

    public MiniSeries getSeries(Long id) {
        return miniSeriesRepository.findById(id).orElse(null);
    }
}
