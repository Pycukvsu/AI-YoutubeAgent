package com.youtubeagent.repository;

import com.youtubeagent.entity.AbTest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AbTestRepository extends JpaRepository<AbTest, Long> {
    List<AbTest> findByStatus(String status);
    List<AbTest> findByTestType(String testType);
}
