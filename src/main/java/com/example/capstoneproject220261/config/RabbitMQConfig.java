package com.example.capstoneproject220261.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String ANALYSIS_QUEUE = "analysis.result.queue";
  public static final String ANALYSIS_EXCHANGE = "analysis.exchange";
  public static final String ANALYSIS_ROUTING_KEY = "analysis.result";

  @Bean
  public Queue analysisQueue() {
    return new Queue(ANALYSIS_QUEUE, true);
  }

  @Bean
  public DirectExchange analysisExchange() {
    return new DirectExchange(ANALYSIS_EXCHANGE);
  }

  @Bean
  public Binding analysisBinding() {
    return BindingBuilder
        .bind(analysisQueue())
        .to(analysisExchange())
        .with(ANALYSIS_ROUTING_KEY);
  }

  @Bean
  public MessageConverter messageConverter() {
    return new JacksonJsonMessageConverter();
  }
}
