package com.example.capstoneproject220261.repository;

import com.example.capstoneproject220261.domain.Video;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

  Optional<Video> findByJobId(String jobId);
  Optional<Video> findByS3Key(String s3Key);
}
