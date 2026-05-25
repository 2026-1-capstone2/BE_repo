package com.example.capstoneproject220261.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE = "analysis.exchange";
  public static final String COMPLETED_QUEUE = "analysis.complted.queue";
  public static final String ROUTING_KEY = "analysis.completed";

  //AI 서버가 분석 결과를 발행할 큐. Spring에서 이 큐를 구독(Consumer)해서 결과를 받는다.
  @Bean
  public Queue analysisCompletedQueue() {
    return QueueBuilder.durable(COMPLETED_QUEUE).build();
  }

  //Direct Exchange - routing key가 정확히 일치하는 큐로만 전달한다.
  @Bean
  public DirectExchange analysisExchange() {
    return new DirectExchange(EXCHANGE);
  }

  //Exchange와 Queue를 routing key로 연결
  @Bean
  public Binding completedBinding(
      Queue analysisCompletedQueue,
      DirectExchange analysisExchange) {
    return BindingBuilder.bind(analysisCompletedQueue)
        .to(analysisExchange)
        .with(ROUTING_KEY);
  }

  //메서지를 JSON으로 직렬화/역직렬화. AI 서버가 JSON으로 보내면 Spring이 DTO로 자동 변환.
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory,
      MessageConverter jsonMessageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter);
    return template;
  }
}
