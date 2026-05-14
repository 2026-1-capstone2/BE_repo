package com.example.capstoneproject220261.consumer;

import com.example.capstoneproject220261.config.RabbitMQConfig;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisConsumer {

  @RabbitListener(queues = RabbitMQConfig.ANALYSIS_QUEUE)
  public void consume(Map<String, Object> message) {
    log.info("분석 결과 수신 - message: {}", message);

    String jobId = (String) message.get("job_id");
    Object result = message.get("result");

    log.info("jobId: {}, result: {}", jobId, result);
  }
}
