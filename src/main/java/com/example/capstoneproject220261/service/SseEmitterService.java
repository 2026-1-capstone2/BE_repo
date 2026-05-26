package com.example.capstoneproject220261.service;

import com.example.capstoneproject220261.dto.AnalysisResultResponseDto;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SseEmitterService {

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  private static final long SSE_TIMEOUT = 10 * 60 * 1000L;

  public SseEmitter subscribe(String jobId) {
    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

    emitter.onCompletion(() -> {
      log.info("SSE 완료 - jobId: {}", jobId);
      emitters.remove(jobId);
    });

    emitter.onTimeout(() -> {
      log.info("SSE 타임아웃 - jobId: {}", jobId);
      emitters.remove(jobId);
    });

    emitter.onError(e -> {
      log.warn("SSE 에러 - jobId: {}, msg: {}", jobId, e.getMessage());
      emitters.remove(jobId);
    });

    emitters.put(jobId, emitter);

    try {
      emitter.send(SseEmitter.event()
          .name("connected")
          .data(Map.of("jobId", jobId, "message", "SSE 연결 성공")));
    } catch (IOException e) {
      log.error("초기 SSE 전송 실패 - jobId: {}", jobId, e);
      emitters.remove(jobId);
    }
    log.info("SSE 구독 시작 - jobId: {} (현재 {}개 연결)", jobId, emitters.size());
    return emitter;
  }

  public void sendResult(String jobId, AnalysisResultResponseDto result) {
    SseEmitter emitter = emitters.get(jobId);
    if (emitter == null) {
      log.warn("SSE 구독자 없음 - jobId: {} (DB엔 저장됨, 폴백 조회 가능)", jobId);
      emitters.remove(jobId);
      return;
    }

    try {
      emitter.send(SseEmitter.event()
                             .name("analysis-result")
                             .data(result));
      emitter.complete();
      emitters.remove(jobId);

      log.info("SSE 결과 전송 완료 - jobId: {}", jobId);
    } catch (IOException e) {
      log.error("SSE 결과 전송 실패 - jobId: {}", jobId, e);
      emitter.completeWithError(e);
    }
  }
}
