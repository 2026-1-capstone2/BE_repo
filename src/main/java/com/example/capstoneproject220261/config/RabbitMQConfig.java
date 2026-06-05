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
  public static final String COMPLETED_QUEUE = "analysis.completed.queue";
  public static final String ROUTING_KEY = "analysis.completed";

  @Bean
  public Queue analysisCompletedQueue() {
    return QueueBuilder.durable(COMPLETED_QUEUE).build();
  }

  @Bean
  public DirectExchange analysisExchange() {
    return new DirectExchange(EXCHANGE);
  }

  @Bean
  public Binding completedBinding(
      Queue analysisCompletedQueue,
      DirectExchange analysisExchange) {
    return BindingBuilder.bind(analysisCompletedQueue)
        .to(analysisExchange)
        .with(ROUTING_KEY);
  }

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
