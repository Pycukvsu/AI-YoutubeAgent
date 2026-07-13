package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.entity.MiniSeries;
import com.youtubeagent.entity.Video;
import com.youtubeagent.exception.ExternalServiceException;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineOrchestrator;
import com.youtubeagent.repository.MiniSeriesRepository;
import com.youtubeagent.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.nio.file.Paths;

@Service
public class MiniSeriesService {

    private static final Logger log = LoggerFactory.getLogger(MiniSeriesService.class);

    private final OpenAiService openAiService;
    private final DalleService dalleService;
    private final FfmpegService ffmpegService;
    private final EdgeTtsService edgeTtsService;
    private final MiniSeriesRepository miniSeriesRepository;
    private final VideoRepository videoRepository;
    private final ObjectMapper objectMapper;

    public MiniSeriesService(OpenAiService openAiService,
                              DalleService dalleService,
                              FfmpegService ffmpegService,
                              EdgeTtsService edgeTtsService,
                              MiniSeriesRepository miniSeriesRepository,
                              VideoRepository videoRepository,
                              ObjectMapper objectMapper) {
        this.openAiService = openAiService;
        this.dalleService = dalleService;
        this.ffmpegService = ffmpegService;
        this.edgeTtsService = edgeTtsService;
        this.miniSeriesRepository = miniSeriesRepository;
        this.videoRepository = videoRepository;
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

    public void generateEpisodeVideo(Long seriesId) {
        try {
            MiniSeries series = miniSeriesRepository.findById(seriesId)
                    .orElseThrow(() -> new ExternalServiceException("MiniSeries", "Series not found"));

            Map<String, Object> episode = getNextEpisode(seriesId);
            if (episode == null) {
                log.info("No more episodes for series '{}'", series.getTitle());
                return;
            }

            Video video = new Video(
                    episode.get("seriesTitle") + " - " + episode.get("episodeTitle"),
                    null
            );
            video.setDescription("Серия " + episode.get("episodeNumber") + " из " + episode.get("totalEpisodes"));
            video = videoRepository.save(video);

            String tempDir = "/tmp/youtube-agent/video_" + video.getId();
            String outputDir = tempDir + "/scenes";
            java.nio.file.Files.createDirectories(Paths.get(outputDir));

            String sceneDesc = (String) episode.get("sceneDescription");
            String scriptText = (String) episode.get("scriptText");
            String hook = (String) episode.get("hook");
            String cliffhanger = (String) episode.get("cliffhanger");
            String fullScript = hook + "\n\n" + scriptText + "\n\n" + cliffhanger;

            log.info("Generating scene image for episode {}", episode.get("episodeNumber"));
            String imagePath;
            try {
                imagePath = dalleService.generateSceneImage(sceneDesc, (String) episode.get("episodeTitle"), 1, outputDir);
            } catch (Exception e) {
                log.warn("DALL-E failed, using Pexels image: {}", e.getMessage());
                imagePath = downloadPexelsImage(sceneDesc, outputDir);
            }

            log.info("Converting to video");
            String clipPath = outputDir + "/clip.mp4";
            if (imagePath.endsWith(".mp4")) {
                ffmpegService.trimClip(imagePath, clipPath, 0, 30);
            } else {
                ffmpegService.imageToVideo(imagePath, clipPath, 5.0);
            }

            log.info("Generating TTS audio");
            String audioPath = tempDir + "/voiceover.mp3";
            edgeTtsService.generateAudio(fullScript, audioPath);

            log.info("Assembling final video");
            String finalPath = tempDir + "/final.mp4";
            ffmpegService.assembleFull(outputDir, audioPath, null, null, finalPath);

            video.setFilePath(finalPath);
            video.setTitle((String) episode.get("seriesTitle") + " Эп." + episode.get("episodeNumber") + ": " + episode.get("episodeTitle"));
            video.setStatus("assembled");
            videoRepository.save(video);

            log.info("Episode video generated: {}", finalPath);

        } catch (Exception e) {
            log.error("Failed to generate episode video: {}", e.getMessage());
            throw new ExternalServiceException("MiniSeries", e);
        }
    }

    public List<MiniSeries> getAllSeries() {
        return miniSeriesRepository.findAll();
    }

    public MiniSeries getSeries(Long id) {
        return miniSeriesRepository.findById(id).orElse(null);
    }

    private String downloadPexelsImage(String query, String outputDir) {
        try {
            PexelsService pexelsService = new PexelsService(null, objectMapper);
            java.lang.reflect.Field configField = PexelsService.class.getDeclaredField("config");
            configField.setAccessible(true);

            com.youtubeagent.config.PexelsConfig pexelsConfig = new com.youtubeagent.config.PexelsConfig();
            pexelsConfig.setApiKey(System.getenv("PEXELS_API_KEY"));
            pexelsConfig.setBaseUrl("https://api.pexels.com");
            pexelsConfig.setMinDurationSeconds(5);

            PexelsService tempPexels = new PexelsService(pexelsConfig, objectMapper);
            var clips = tempPexels.searchVideos(query, 1);
            if (!clips.isEmpty()) {
                return tempPexels.downloadClip(clips.get(0), outputDir);
            }

            clips = tempPexels.searchVideos("abstract", 1);
            if (!clips.isEmpty()) {
                return tempPexels.downloadClip(clips.get(0), outputDir);
            }

            throw new RuntimeException("No images found");

        } catch (Exception e) {
            log.error("Failed to download Pexels image: {}", e.getMessage());
            throw new ExternalServiceException("Pexels", e);
        }
    }
}
