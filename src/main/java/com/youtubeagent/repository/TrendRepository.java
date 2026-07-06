package com.youtubeagent.repository;

import com.youtubeagent.entity.Trend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrendRepository extends JpaRepository<Trend, Long> {
    List<Trend> findByStatusOrderByDiscoveredAtDesc(String status);
    Optional<Trend> findFirstByStatusOrderBySearchVolumeDesc(String status);
    long countByStatus(String status);
}
