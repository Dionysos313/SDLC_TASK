package com.example.taskmanager.controller;

import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GlobalExceptionHandler.
 */
@WebMvcTest(TaskController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void handleResourceNotFoundException_shouldReturn404WithErrorDetails() throws Exception {
        when(taskService.getTaskById(999L))
                .thenThrow(new ResourceNotFoundException("Task with id 999 not found"));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Task with id 999 not found")))
                .andExpect(jsonPath("$.path", is("/api/tasks/999")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturn400WithValidationErrors() throws Exception {
        Task invalidTask = new Task();
        invalidTask.setTitle(""); // Blank title violates @NotBlank

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.message", containsString("validation")))
                .andExpect(jsonPath("$.validationErrors", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.title", notNullValue()));
    }

    @Test
    void handleMethodArgumentNotValidException_withTitleTooLong_shouldReturnValidationError() throws Exception {
        Task invalidTask = new Task();
        invalidTask.setTitle("A".repeat(101)); // Exceeds max length

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title", containsString("100 characters")));
    }

    @Test
    void handleMethodArgumentNotValidException_withDescriptionTooLong_shouldReturnValidationError() throws Exception {
        Task invalidTask = new Task();
        invalidTask.setTitle("Valid Title");
        invalidTask.setDescription("D".repeat(501)); // Exceeds max length

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.description", containsString("500 characters")));
    }

    @Test
    void handleGenericException_shouldReturn500() throws Exception {
        when(taskService.getAllTasks())
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", containsString("unexpected error")));
    }
}