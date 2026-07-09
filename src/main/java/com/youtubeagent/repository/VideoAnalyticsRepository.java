package com.youtubeagent.repository;

import com.youtubeagent.entity.VideoAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VideoAnalyticsRepository extends JpaRepository<VideoAnalytics, Long> {
    List<VideoAnalytics> findByVideoIdOrderByRecordedAtDesc(Long videoId);
    List<VideoAnalytics> findByVariantOrderByRecordedAtDesc(String variant);
    List<VideoAnalytics> findByVideoIdAndVariantOrderByRecordedAtDesc(Long videoId, String variant);
}
