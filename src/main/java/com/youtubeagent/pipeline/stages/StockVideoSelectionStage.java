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
                    For the topic "%s" generate a short English search query for stock video sites.
                    The query must describe the VISUAL aspect of the topic.
                    Answer with ONLY the search query text, no quotes, no extra text.
                    Example: "space stars galaxy" or "cooking kitchen food"
                    Language: English only!
                    """.formatted(topic);

            Map<String, Object> response = openAiService.callOpenAi(prompt, 50);
            String query = ((String) response.get("content")).trim().replace("\"", "").toLowerCase();

            log.info("Generated search query for '{}': '{}'", topic, query);
            return query;

        } catch (Exception e) {
            log.warn("Failed to generate search query, using English fallback: {}", e.getMessage());
            return generateFallbackQuery(topic);
        }
    }

    private String generateFallbackQuery(String topic) {
        String lower = topic.toLowerCase();
        if (lower.contains("космос") || lower.contains("mars") || lower.contains("space")) return "space stars galaxy nebula";
        if (lower.contains("наук") || lower.contains("science")) return "laboratory science experiment";
        if (lower.contains("истор") || lower.contains("history")) return "ancient ruins castle history";
        if (lower.contains("психолог") || lower.contains("psychology")) return "brain mind thinking abstract";
        if (lower.contains("природ") || lower.contains("nature")) return "mountains ocean sunset landscape";
        if (lower.contains("еда") || lower.contains("food") || lower.contains("рецепт")) return "cooking kitchen food preparation";
        if (lower.contains("спорт") || lower.contains("sport")) return "athletics running sports action";
        if (lower.contains("путешеств") || lower.contains("travel")) return "travel airplane city aerial";
        if (lower.contains("лайфхак") || lower.contains("lifehack")) return "technology gadgets modern";
        if (lower.contains("загадк") || lower.contains("mystery")) return "mysterious fog dark atmosphere";
        return "abstract colorful dynamic motion";
    }
}
