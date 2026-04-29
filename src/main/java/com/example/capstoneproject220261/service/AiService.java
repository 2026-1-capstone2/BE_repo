package com.example.capstoneproject220261.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {
  private final WebClient webClient;

  public String requestAnalyze(String jobId, String userId, String videoUrl) {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("job_id", jobId);
    requestBody.put("user_id", userId);
    requestBody.put("video.url", videoUrl);

    return webClient
        .post()
        .uri("/api/v1/analyze")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  public String getJobStatus(String jobId) {
    return webClient
        .get()
        .uri("/api/v1/jobs" + jobId)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }
}
