package com.youtubeagent.repository;

import com.youtubeagent.entity.MiniSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MiniSeriesRepository extends JpaRepository<MiniSeries, Long> {
    List<MiniSeries> findByStatus(String status);
    MiniSeries findFirstByStatusOrderByCreatedAtDesc(String status);
}
