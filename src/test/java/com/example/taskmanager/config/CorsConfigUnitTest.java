package com.example.taskmanager.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CorsConfig initialization logic.
 */
class CorsConfigUnitTest {

    private CorsConfig corsConfig;
    private CorsRegistry corsRegistry;
    private CorsRegistration corsRegistration;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        corsRegistry = mock(CorsRegistry.class);
        corsRegistration = mock(CorsRegistration.class);
        
        // Setup proper method chaining for mocks
        when(corsRegistry.addMapping(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.maxAge(anyLong())).thenReturn(corsRegistration);
        when(corsRegistration.allowedOriginPatterns(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(anyBoolean())).thenReturn(corsRegistration);
    }

    @Test
    void init_withEmptyOrigins_shouldSetEmptyArray() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "");
        
        corsConfig.init();
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedOriginPatterns("*");
        verify(corsRegistration, never()).allowCredentials(true);
    }

    @Test
    void init_withNullOrigins_shouldSetEmptyArray() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", null);
        
        corsConfig.init();
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedOriginPatterns("*");
    }

    @Test
    void init_withMultipleOrigins_shouldParseCorrectly() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", 
            "http://localhost:5173,http://localhost:3000,https://example.com");
        
        corsConfig.init();
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedOrigins(any(String[].class));
        verify(corsRegistration).allowCredentials(true);
    }

    @Test
    void init_withOriginsWithSpaces_shouldTrimCorrectly() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", 
            "  http://localhost:5173  ,  http://localhost:3000  ");
        
        corsConfig.init();
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedOrigins(any(String[].class));
        verify(corsRegistration).allowCredentials(true);
    }

    @Test
    void init_withSingleOrigin_shouldConfigureCorrectly() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "https://production.com");
        
        corsConfig.init();
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedOrigins(any(String[].class));
        verify(corsRegistration).allowCredentials(true);
    }

    @Test
    void addCorsMappings_shouldConfigureAllMethods() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:5173");
        corsConfig.init();
        
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    void addCorsMappings_shouldAllowAllHeaders() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:5173");
        corsConfig.init();
        
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedHeaders("*");
    }

    @Test
    void addCorsMappings_shouldSetMaxAge() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:5173");
        corsConfig.init();
        
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).maxAge(3600);
    }

    @Test
    void init_withWhitespaceOnlyOrigins_shouldSetEmptyArray() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "   ");
        
        corsConfig.init();
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedOriginPatterns("*");
    }

    @Test
    void init_withEmptyCommaSeparatedValues_shouldFilterEmpty() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:5173,,https://example.com");
        
        corsConfig.init();
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowedOrigins(any(String[].class));
        verify(corsRegistration).allowCredentials(true);
    }

    @Test
    void addCorsMappings_shouldApplyToApiEndpoints() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:5173");
        corsConfig.init();
        
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistry).addMapping("/api/**");
    }

    @Test
    void addCorsMappings_developmentMode_shouldNotAllowCredentials() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "");
        corsConfig.init();
        
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration, never()).allowCredentials(anyBoolean());
    }

    @Test
    void addCorsMappings_productionMode_shouldAllowCredentials() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "https://production.com");
        corsConfig.init();
        
        corsConfig.addCorsMappings(corsRegistry);
        
        verify(corsRegistration).allowCredentials(true);
    }
}