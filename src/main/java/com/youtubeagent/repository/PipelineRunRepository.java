package com.youtubeagent.repository;

import com.youtubeagent.entity.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {
    List<PipelineRun> findByVideoIdOrderByStartedAtDesc(Long videoId);
    List<PipelineRun> findByStatus(String status);
}
