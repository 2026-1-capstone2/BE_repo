package com.example.capstoneproject220261.repository;

import com.example.capstoneproject220261.domain.AnalysisResult;
import com.example.capstoneproject220261.domain.Video;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

  Optional<AnalysisResult> findByVideo(Video video);
  Optional<AnalysisResult> findByVideo_JobId(String jobId);
}
