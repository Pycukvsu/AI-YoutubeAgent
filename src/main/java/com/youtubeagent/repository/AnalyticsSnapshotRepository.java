package com.youtubeagent.repository;

import com.youtubeagent.entity.AnalyticsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshot, Long> {
    List<AnalyticsSnapshot> findByVideoIdOrderBySnapshotAtDesc(Long videoId);
    List<AnalyticsSnapshot> findByYoutubeVideoId(String youtubeVideoId);
}
