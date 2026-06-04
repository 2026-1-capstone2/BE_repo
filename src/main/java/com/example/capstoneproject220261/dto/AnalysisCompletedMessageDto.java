package com.example.capstoneproject220261.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// AI 서버의 실제 발행 포맷에 맞춤 (2026-06-04 합의)
// 성공: job_id, status="completed", spatial_features_s3_key, duration_ms, completed_at
// 실패: job_id, status="failed", error{code, message}, failed_at
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