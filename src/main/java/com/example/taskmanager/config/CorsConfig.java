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

  @Value("${app.cors.allowed-origins:}")
  private String allowedOrigins;

  private String[] originsArray;

  @PostConstruct
  public void init() {
    if (allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
      originsArray = new String[0];
      log.warn("No CORS origins configured. Using permissive settings for development.");
    } else {
      originsArray = Arrays.stream(allowedOrigins.split(","))
                           .map(String::trim)
                           .filter(s -> !s.isEmpty())
                           .toArray(String[]::new);
      log.info("CORS allowed origins: {}", String.join(", ", originsArray));
    }
  }         

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    CorsRegistration registration = registry.addMapping("/api/**")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);

    if (originsArray == null || originsArray.length == 0) {
      // Development mode: allow all origins WITHOUT credentials
      registration.allowedOriginPatterns("*");
      log.warn("Applied permissive CORS for /api/** (development only - no credentials)");
    } else {
      // Production mode: specific origins WITH credentials
      registration.allowedOrigins(originsArray)
                  .allowCredentials(true);
      log.info("Applied CORS mapping for /api/** with credentials");
    }
  }
}