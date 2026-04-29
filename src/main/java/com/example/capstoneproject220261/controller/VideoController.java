package com.example.capstoneproject220261.controller;
import com.example.capstoneproject220261.service.AiService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

  private final AiService aiService;

  @PostMapping("/analyze")
  public ResponseEntity<Map<String, Object>> requestAnalyze(
      @RequestBody Map<String, String> request) {

    String jobId = UUID.randomUUID().toString();
    String userId = request.get("userId");
    String videoUrl = request.get("videoUrl");

    log.info("영상 분석 요청 - jobId: {}, userId: {}", jobId, userId);

    String aiResponse = aiService.requestAnalyze(jobId, userId, videoUrl);
    log.info("서버 요청 응답 - jobId: {}, response: {}", jobId, aiResponse);

    Map<String, Object> response = new HashMap<>();
    response.put("jobId", jobId);
    response.put("status", "accepted");
    response.put("message", "분석 요청 접수");

    return ResponseEntity.ok(response);
  }

  @GetMapping("/status/{jobId}")
  public ResponseEntity<String> getStatus(
      @PathVariable String jobId) {

    log.info("작업 상태 확인 요청 - jobId: {}", jobId);
    String status = aiService.getJobStatus(jobId);
    return ResponseEntity.ok(status);
  }
}
