package com.youtubeagent.pipeline.stages;

import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineStage;
import com.youtubeagent.pipeline.PipelineStageException;
import com.youtubeagent.service.OpenAiService;
import com.youtubeagent.service.PexelsService;
import com.youtubeagent.util.FileUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class StockVideoSelectionStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(StockVideoSelectionStage.class);

    private final PexelsService pexelsService;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Value("${pipeline.temp-dir:/tmp/youtube-agent}")
    private String tempDir;

    @Value("${pexels.clips-per-video:4}")
    private int clipsPerVideo;

    public StockVideoSelectionStage(PexelsService pexelsService, OpenAiService openAiService, ObjectMapper objectMapper) {
        this.pexelsService = pexelsService;
        this.openAiService = openAiService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "stock_video_selection";
    }

    @Override
    public void execute(PipelineContext context) throws PipelineStageException {
        try {
            Video video = context.getVideo();
            String topic = context.getTrend() != null ? context.getTrend().getTopic() : "nature";
            String clipsDir = Paths.get(tempDir, "video_" + video.getId(), "clips").toString();
            FileUtils.ensureDir(clipsDir);

            String searchQuery = generateSearchQuery(topic);

            List<PexelsService.VideoClip> clips = pexelsService.searchVideos(searchQuery, clipsPerVideo);

            if (clips.isEmpty()) {
                clips = pexelsService.searchVideos("abstract colorful", clipsPerVideo);
            }

            List<String> downloadedPaths = new ArrayList<>();
            for (PexelsService.VideoClip clip : clips) {
                String path = pexelsService.downloadClip(clip, clipsDir);
                downloadedPaths.add(path);
            }

            context.setVideoClips(downloadedPaths);
            log.info("Downloaded {} video clips for query '{}' to {}", downloadedPaths.size(), searchQuery, clipsDir);
        } catch (PipelineStageException e) {
            throw e;
        } catch (Exception e) {
            throw new PipelineStageException(name(), e);
        }
    }

    private String generateSearchQuery(String topic) {
        try {
            String prompt = """
                    Для темы "%s" придумай короткий поисковый запрос на английском для поиска стоковых видео.
                    Запрос должен описывать ВИЗУАЛЬНУЮ составляющую темы.
                    Ответ — только текст запроса, без кавычек и лишнего.
                    Пример: "space exploration stars" или "cooking food preparation"
                    """.formatted(topic);

            Map<String, Object> response = openAiService.callOpenAi(prompt, 50);
            String query = ((String) response.get("content")).trim().replace("\"", "");

            log.info("Generated search query for '{}': '{}'", topic, query);
            return query;

        } catch (Exception e) {
            log.warn("Failed to generate search query, using topic as-is: {}", e.getMessage());
            return topic;
        }
    }
}
