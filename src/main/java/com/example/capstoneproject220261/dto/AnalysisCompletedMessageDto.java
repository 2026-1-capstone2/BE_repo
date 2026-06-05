package com.example.capstoneproject220261.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisCompletedMessageDto(
    String job_id,
    String user_id,
    String status,
    String event_type,
    String spatial_features_s3_key,
    Long duration_ms,
    String completed_at,
    String failed_at,
    ErrorInfo error
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ErrorInfo(
      String code,
      String message
  ) {}

  public boolean isFailed() {
    return "failed".equalsIgnoreCase(status);
  }
}