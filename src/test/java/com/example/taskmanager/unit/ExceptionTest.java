package com.example.taskmanager.unit;

import com.example.taskmanager.exception.ErrorResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void resourceNotFoundException_withMessage_shouldCreateException() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void resourceNotFoundException_withMessageAndCause_shouldCreateException() {
        String message = "Resource not found";
        Throwable cause = new RuntimeException("Cause");
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void errorResponse_defaultConstructor_shouldSetTimestamp() {
        ErrorResponse response = new ErrorResponse();

        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void errorResponse_parameterizedConstructor_shouldSetAllFields() {
        ErrorResponse response = new ErrorResponse(404, "Not Found", "Resource not found", "/api/tasks/999");

        assertNotNull(response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Resource not found", response.getMessage());
        assertEquals("/api/tasks/999", response.getPath());
    }

    @Test
    void errorResponse_withValidationErrors_shouldSetErrors() {
        ErrorResponse response = new ErrorResponse();
        Map<String, String> errors = new HashMap<>();
        errors.put("title", "Title is required");
        errors.put("dueDate", "Invalid date");

        response.setValidationErrors(errors);

        assertNotNull(response.getValidationErrors());
        assertEquals(2, response.getValidationErrors().size());
        assertEquals("Title is required", response.getValidationErrors().get("title"));
    }

    @Test
    void errorResponse_settersAndGetters_shouldWork() {
        ErrorResponse response = new ErrorResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setTimestamp(now);
        response.setStatus(400);
        response.setError("Bad Request");
        response.setMessage("Validation failed");
        response.setPath("/api/tasks");

        assertEquals(now, response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("/api/tasks", response.getPath());
    }
}