package com.example.taskmanager.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for CORS configuration.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void corsConfiguration_shouldAllowConfiguredOrigins() throws Exception {
        mockMvc.perform(options("/api/tasks")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void corsConfiguration_shouldAllowConfiguredMethods() throws Exception {
        mockMvc.perform(options("/api/tasks")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", 
                    org.hamcrest.Matchers.containsString("POST")));
    }

    @Test
    void corsConfiguration_shouldAllowPatchMethod() throws Exception {
        mockMvc.perform(options("/api/tasks/1/status")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", 
                    org.hamcrest.Matchers.containsString("PATCH")));
    }

    @Test
    void corsConfiguration_shouldAllowDeleteMethod() throws Exception {
        mockMvc.perform(options("/api/tasks/1")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", 
                    org.hamcrest.Matchers.containsString("DELETE")));
    }
}