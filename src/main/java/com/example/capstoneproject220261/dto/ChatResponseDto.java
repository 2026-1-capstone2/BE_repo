package com.example.capstoneproject220261.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatResponseDto(
    String job_id,
    String answer,
    Metadata metadata
) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Metadata(
      Integer tokens_used,
      Integer inference_time_ms,
      Boolean spatial_cache_hit,
      Boolean stub
  ) {}
}
