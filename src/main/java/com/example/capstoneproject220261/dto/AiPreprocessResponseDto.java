package com.example.capstoneproject220261.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiPreprocessResponseDto(
    String job_id,
    String status,
    Integer estimated_time_sec,
    String received_at
) {
}
