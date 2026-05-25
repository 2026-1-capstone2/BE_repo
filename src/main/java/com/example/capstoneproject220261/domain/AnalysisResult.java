package com.example.capstoneproject220261.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "analysis_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "video_id", nullable = false, unique = true)
  private Video video;

  @Column(name = "spatial_features_s3_key", length = 500)
  private String spatialFeaturesS3Key;

  @Column(name = "spatial_confidence", precision = 3, scale = 2)
  private BigDecimal spatialConfidence;

  @Column(name = "video_duration_sec")
  private Integer videoDurationSec;

  @Column(name = "video_resolution", length = 30)
  private String videoResolution;

  @Column(name = "frame_count")
  private Integer frameCount;

  @Column(name = "points_3d")
  private Integer points3D;

  @Column(name = "estimated_type", length = 100)
  private String estimatedType;

  @Column(name = "estimated_area_m2", precision = 8, scale = 2)
  private BigDecimal estimatedAreaM2;

  @Column(name = "detected_objects", columnDefinition = "TEXT")
  private String detectedObjectsCsv;

  @Column(name = "preprocess_ms")
  private Integer preprocessMs;

  @Column(name = "total_ms")
  private Integer totalMs;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  private AnalysisResult(Video video, String spatialFeaturesS3Key, BigDecimal spatialConfidence, Integer videoDurationSec,
      String videoResolution, Integer frameCount, Integer points3D, String estimatedType, BigDecimal estimatedAreaM2,
      List<String> detectedObjects, Integer preprocessMs, Integer totalMs) {
    this.video = video;
    this.spatialFeaturesS3Key = spatialFeaturesS3Key;
    this.spatialConfidence = spatialConfidence;
    this.videoDurationSec = videoDurationSec;
    this.videoResolution = videoResolution;
    this.frameCount = frameCount;
    this.points3D = points3D;
    this.estimatedType = estimatedType;
    this.estimatedAreaM2 = estimatedAreaM2;
    this.detectedObjectsCsv = toCsv(detectedObjects);
    this.preprocessMs = preprocessMs;
    this.totalMs = totalMs;
  }

  public List<String> getDetectedObjects() {
    if (detectedObjectsCsv == null || detectedObjectsCsv.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.asList(detectedObjectsCsv.split(","));
  }

  private static String toCsv(List<String> objects) {
    if (objects == null || objects.isEmpty()) {
      return null;
    }
    return String.join(",", objects);
  }
}
