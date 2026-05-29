package com.example.capstoneproject220261.service;


import com.example.capstoneproject220261.dto.AiPreprocessRequestDto;
import com.example.capstoneproject220261.dto.AiPreprocessResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

  private final WebClient webClient;

  //영상 전처리 의뢰
  public AiPreprocessResponseDto preprocess(AiPreprocessRequestDto request) {
    log.info("AI 서버 전처리 의뢰 - job_id: {}", request.job_id());

    AiPreprocessResponseDto response = webClient.post()
        .uri("/api/v1/preprocess")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(AiPreprocessResponseDto.class)
        .block();

    log.info("AI 서버 응답 - status: {}, 예상 시간: {}초",
        response != null ? response.status() : "null",
        response != null ? response.estimated_time_sec() : "?");

    return response;
  }
}
