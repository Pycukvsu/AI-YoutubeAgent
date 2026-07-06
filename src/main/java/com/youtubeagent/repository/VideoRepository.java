package com.youtubeagent.repository;

import com.youtubeagent.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Page<Video> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Video> findByStatus(String status);
    Optional<Video> findByYoutubeId(String youtubeId);
    List<Video> findByStatusAndScheduledAtBefore(String status, java.time.LocalDateTime now);
}
