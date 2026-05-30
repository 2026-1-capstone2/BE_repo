package com.example.capstoneproject220261.config;


import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                                                                 .entryTtl(Duration.ofMinutes(10))
                                                                 .serializeKeysWith(
                                                                     RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                                                                 )
                                                                 .serializeValuesWith(
                                                                     RedisSerializationContext.SerializationPair.fromSerializer(
                                                                         new GenericJackson2JsonRedisSerializer()
                                                                     )
                                                                 )
                                                                 .disableCachingNullValues();

    return builder -> builder.cacheDefaults(cacheConfig);
  }
}
