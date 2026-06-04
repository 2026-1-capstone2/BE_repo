package com.example.capstoneproject220261.service;

import com.example.capstoneproject220261.config.RabbitMQConfig;
import com.example.capstoneproject220261.dto.AnalysisCompletedMessageDto;
import com.example.capstoneproject220261.dto.AnalysisResultResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisResultConsumer {

  private final VideoService videoService;
  private final SseEmitterService sseEmitterService;

  @RabbitListener(queues = RabbitMQConfig.COMPLETED_QUEUE)
  public void onAnalysisCompleted(AnalysisCompletedMessageDto message) {
    log.info("RabbitMQ 결과 수신 - jobId: {}, status: {}", message.job_id(), message.status());

    AnalysisResultResponseDto response = videoService.handleAnalysisResult(message);
    if (response != null) {
      sseEmitterService.sendResult(message.job_id(), response);
    }
  }
}