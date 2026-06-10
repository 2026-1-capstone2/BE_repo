package com.example.capstoneproject220261.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI(){
    return new OpenAPI()
        .info(new Info()
            .title("Capstone-Project2")
            .description("API 명세서")
            .version("v1.0.0"));
  }
}
