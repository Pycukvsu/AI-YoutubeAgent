package com.youtubeagent.pipeline.stages;

import com.youtubeagent.entity.Trend;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineStage;
import com.youtubeagent.pipeline.PipelineStageException;
import com.youtubeagent.repository.TrendRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TrendResearchStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(TrendResearchStage.class);

    private final TrendRepository trendRepository;

    public TrendResearchStage(TrendRepository trendRepository) {
        this.trendRepository = trendRepository;
    }

    @Override
    public String name() {
        return "trend_research";
    }

    @Override
    public void execute(PipelineContext context) throws PipelineStageException {
        try {
            Trend trend = context.getTrend();
            if (trend == null) {
                trend = trendRepository.findFirstByStatusOrderBySearchVolumeDesc("new")
                        .orElseGet(() -> {
                            Trend manual = new Trend("Interesting facts about technology", "manual");
                            return trendRepository.save(manual);
                        });
                context.setTrend(trend);
            }

            trend.setStatus("used");
            trend.setUsedAt(java.time.LocalDateTime.now());
            trendRepository.save(trend);

            log.info("Selected trend: '{}' (source: {})", trend.getTopic(), trend.getSource());
        } catch (Exception e) {
            throw new PipelineStageException(name(), e);
        }
    }
}
