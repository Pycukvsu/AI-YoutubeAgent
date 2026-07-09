package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeoService {

    private static final Logger log = LoggerFactory.getLogger(SeoService.class);

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    public SeoService(OpenAiService openAiService, ObjectMapper objectMapper) {
        this.openAiService = openAiService;
        this.objectMapper = objectMapper;
    }

    public SeoResult optimize(String topic, String language) {
        try {
            String prompt = """
                    Ты — YouTube SEO эксперт. Оптимизируй контент для максимального охвата.
                    Язык: %s
                    Тема: %s

                    Формат ответа — строго JSON:
                    {
                        "title": "оптимизированный заголовок (макс 70 символов, с ключевым словом в начале)",
                        "description": "описание (150-300 символов, с ключевыми словами, хештегами и CTA)",
                        "tags": ["тег1", "тег2", "...", "тег10-15"],
                        "hashtags": ["#хештег1", "#хештег2", "...", "#хештег5-8"],
                        "keywords": ["ключевое слово1", "ключевое слово2", "..."],
                        "seo_score": 85,
                        "tips": ["совет1", "совет2"]
                    }

                    Правила SEO:
                    - Заголовок: ключевое слово в начале, интрига, эмодзи допустимы
                    - Описание: первые 2 строки решают (видны без "ещё"), CTA в конце
                    - Теги: 10-15 штук, от общих к конкретным, включая ошибки
                    - Хештеги: 5-8 штук, популярные + нишевые
                    - Ключевые слова: что ищут люди в YouTube
                    - Избегай спама и перенасыщения ключевиками
                    """.formatted(language, topic);

            Map<String, Object> aiResult = openAiService.callOpenAi(prompt, 800);
            String content = (String) aiResult.get("content");
            JsonNode seoJson = objectMapper.readTree(content);

            SeoResult result = new SeoResult();
            result.setTitle(seoJson.path("title").asText());
            result.setDescription(seoJson.path("description").asText());
            result.setSeoScore(seoJson.path("seo_score").asInt(0));

            JsonNode tagsNode = seoJson.path("tags");
            if (tagsNode.isArray()) {
                List<String> tags = new ArrayList<>();
                tagsNode.forEach(t -> tags.add(t.asText()));
                result.setTags(tags);
            }

            JsonNode hashtagsNode = seoJson.path("hashtags");
            if (hashtagsNode.isArray()) {
                List<String> hashtags = new ArrayList<>();
                hashtagsNode.forEach(h -> hashtags.add(h.asText()));
                result.setHashtags(hashtags);
            }

            JsonNode keywordsNode = seoJson.path("keywords");
            if (keywordsNode.isArray()) {
                List<String> keywords = new ArrayList<>();
                keywordsNode.forEach(k -> keywords.add(k.asText()));
                result.setKeywords(keywords);
            }

            JsonNode tipsNode = seoJson.path("tips");
            if (tipsNode.isArray()) {
                List<String> tips = new ArrayList<>();
                tipsNode.forEach(t -> tips.add(t.asText()));
                result.setTips(tips);
            }

            log.info("SEO optimization complete for '{}', score: {}", topic, result.getSeoScore());
            return result;

        } catch (Exception e) {
            log.error("SEO optimization failed: {}", e.getMessage());
            throw new ExternalServiceException("SEO", e);
        }
    }

    public List<String> suggestTitles(String topic, int count) {
        try {
            String prompt = """
                    Придумай %d вариантов заголовков для YouTube Shorts на тему: "%s"
                    Язык: русский
                    Правила: интрига, ключевое слово в начале, максимум 70 символов, с эмодзи

                    Формат: {"titles": ["заголовок1", "заголовок2", ...]}
                    """.formatted(count, topic);

            Map<String, Object> aiResult = openAiService.callOpenAi(prompt, 500);
            String content = (String) aiResult.get("content");
            JsonNode json = objectMapper.readTree(content);

            List<String> titles = new ArrayList<>();
            JsonNode titlesNode = json.path("titles");
            if (titlesNode.isArray()) {
                titlesNode.forEach(t -> titles.add(t.asText()));
            }

            return titles;

        } catch (Exception e) {
            log.error("Title suggestion failed: {}", e.getMessage());
            throw new ExternalServiceException("SEO", e);
        }
    }

    public static class SeoResult {
        private String title;
        private String description;
        private List<String> tags = new ArrayList<>();
        private List<String> hashtags = new ArrayList<>();
        private List<String> keywords = new ArrayList<>();
        private List<String> tips = new ArrayList<>();
        private int seoScore;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public List<String> getHashtags() { return hashtags; }
        public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }

        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }

        public int getSeoScore() { return seoScore; }
        public void setSeoScore(int seoScore) { this.seoScore = seoScore; }
    }
}
