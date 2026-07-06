package com.youtubeagent.repository;

import com.youtubeagent.entity.Script;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScriptRepository extends JpaRepository<Script, Long> {
    Script findByVideoId(Long videoId);
}
