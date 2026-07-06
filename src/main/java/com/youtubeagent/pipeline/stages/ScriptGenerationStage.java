package com.youtubeagent.pipeline.stages;

import com.youtubeagent.dto.ScriptRequest;
import com.youtubeagent.dto.ScriptResponse;
import com.youtubeagent.entity.Script;
import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineStage;
import com.youtubeagent.pipeline.PipelineStageException;
import com.youtubeagent.repository.ScriptRepository;
import com.youtubeagent.repository.VideoRepository;
import com.youtubeagent.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScriptGenerationStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(ScriptGenerationStage.class);

    private final OpenAiService openAiService;
    private final ScriptRepository scriptRepository;
    private final VideoRepository videoRepository;

    public ScriptGenerationStage(OpenAiService openAiService,
                                  ScriptRepository scriptRepository,
                                  VideoRepository videoRepository) {
        this.openAiService = openAiService;
        this.scriptRepository = scriptRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    public String name() {
        return "script_generation";
    }

    @Override
    public void execute(PipelineContext context) throws PipelineStageException {
        try {
            String topic = context.getTrend() != null ? context.getTrend().getTopic() : "Interesting facts";

            ScriptRequest request = new ScriptRequest();
            request.setTopic(topic);
            request.setTone("informative");
            request.setLanguage("ru");
            request.setTargetDurationSeconds(45);

            ScriptResponse response = openAiService.generateScript(request);

            Video video = context.getVideo();
            video.setTitle(response.getTitle());
            video.setDescription(response.getDescription());
            video.setTags(response.getTags());
            videoRepository.save(video);

            Script script = new Script(video, response.getScriptText());
            script.setWordCount(response.getScriptText().split("\\s+").length);
            script.setDurationEstimateSeconds(response.getDurationEstimateSeconds());
            script.setTone("informative");
            script = scriptRepository.save(script);

            context.setScript(script);
            context.setMetadata("duration_estimate", response.getDurationEstimateSeconds());

            log.info("Script generated: {} words, ~{}s", script.getWordCount(), script.getDurationEstimateSeconds());
        } catch (PipelineStageException e) {
            throw e;
        } catch (Exception e) {
            throw new PipelineStageException(name(), e);
        }
    }
}
