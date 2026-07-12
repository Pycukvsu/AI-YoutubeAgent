package com.youtubeagent.repository;

import com.youtubeagent.entity.Trend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TrendRepository extends JpaRepository<Trend, Long> {
    List<Trend> findByStatusOrderByDiscoveredAtDesc(String status);
    Optional<Trend> findFirstByStatusOrderBySearchVolumeDesc(String status);
    long countByStatus(String status);
    Optional<Trend> findByTopicIgnoreCaseAndStatus(String topic, String status);

    @Query(value = "SELECT * FROM trend WHERE status = 'new' ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Trend> findRandomByStatus(String status);
}
