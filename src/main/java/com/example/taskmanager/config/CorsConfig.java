package com.example.taskmanager.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

  @Value("${app.cors.allowed-origins:*}")
  private String allowedOrigins;

  private String[] originsArray;

  @PostConstruct
  public void init() {
    originsArray = Arrays.stream(allowedOrigins.split(","))
                         .map(String::trim)
                         .filter(s -> !s.isEmpty())
                         .toArray(String[]::new);

    if (originsArray.length == 0) {
      log.info("CORS configured with no origins. Using wildcard '*' for development.");
    } else {
      log.info("CORS allowed origins: {}", String.join(", ", originsArray));
    }
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    if (originsArray == null || originsArray.length == 0) {
      // For quick testing - allow all (NOT recommended for production)
      registry.addMapping("/api/**")
              .allowedOrigins("*")
              .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
      log.warn("Applied wildcard CORS for /api/** (development only).");
    } else {
      registry.addMapping("/api/**")
              .allowedOrigins(originsArray)
              .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
              .allowCredentials(true);
      log.info("Applied CORS mapping for /api/**");
    }
  }
}