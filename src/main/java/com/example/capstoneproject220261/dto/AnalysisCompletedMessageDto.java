package com.example.capstoneproject220261.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisCompletedMessageDto(
    String job_id,
    String user_id,
    String status,
    String completed_at,
    String failed_at,
    Result result,
    Error error
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Result(
      String spatial_features_s3_key,
      Double spatial_confidence,
      VideoMetadata video_metadata,
      SpaceAnalysis space_analysis,
      ProcessingTime processing_time
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record VideoMetadata(
      Integer duration_sec,
      String resolution,
      Integer frame_count,
      Integer points_3d
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SpaceAnalysis(
      String estimated_type,
      Double estimated_area_m2,
      List<String> detected_objects
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ProcessingTime(
      Integer preprocess_ms,
      Integer total_ms
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Error(
      String code,
      String message,
      String details
  ) {}

  public boolean isCompleted() {
    return "completed".equalsIgnoreCase(status);
  }

  public boolean isFailed() {
    return "failed".equalsIgnoreCase(status);
  }
}
