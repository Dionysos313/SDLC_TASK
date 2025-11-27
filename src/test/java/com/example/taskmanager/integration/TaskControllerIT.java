package com.example.taskmanager.integration;

import com.example.taskmanager.exception.ErrorResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TaskController.
 * Tests the full stack from HTTP to database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskControllerIT {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        repository.deleteAll();
    }

    @Test
    @Order(1)
    void shouldCreateTask() {
        Task task = new Task();
        task.setTitle("IT Task");
        task.setDescription("Integration test");
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(LocalDate.now().plusDays(2));

        ResponseEntity<Task> response = rest.postForEntity("/api/tasks", task, Task.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verify in database instead of relying on response body
        var tasks = repository.findAll();
        assertFalse(tasks.isEmpty(), "Task should be created in database");
        Task created = tasks.get(0);
        assertEquals("IT Task", created.getTitle());
        assertEquals("Integration test", created.getDescription());
        assertEquals(TaskStatus.TODO, created.getStatus());
        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    @Order(2)
    void shouldGetTaskById() {
        // Create a task first
        Task task = repository.save(createSampleTask());

        ResponseEntity<Task> response = rest.getForEntity("/api/tasks/" + task.getId(), Task.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(task.getId(), response.getBody().getId());
    }

    @Test
    void shouldGetAllTasks() {
        repository.save(createSampleTask());
        repository.save(createSampleTask());

        ResponseEntity<Task[]> response = rest.getForEntity("/api/tasks", Task[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 2);
    }

    @Test
    void shouldUpdateTask() {
        Task task = repository.save(createSampleTask());

        task.setTitle("Updated Title");
        task.setStatus(TaskStatus.IN_PROGRESS);

        HttpEntity<Task> requestEntity = new HttpEntity<>(task);
        ResponseEntity<Task> response = rest.exchange(
            "/api/tasks/" + task.getId(),
            HttpMethod.PUT,
            requestEntity,
            Task.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Title", response.getBody().getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, response.getBody().getStatus());
    }

    @Test
    void shouldDeleteTask() {
        Task task = repository.save(createSampleTask());

        ResponseEntity<Void> response = rest.exchange(
            "/api/tasks/" + task.getId(),
            HttpMethod.DELETE,
            null,
            Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(repository.existsById(task.getId()));
    }

    @Test
    void shouldReturnNotFoundForInvalidId() {
        // Use ErrorResponse to properly deserialize error responses
        ResponseEntity<ErrorResponse> response = rest.exchange(
            "/api/tasks/9999",
            HttpMethod.GET,
            null,
            ErrorResponse.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void shouldValidateTitleRequired() {
        Task task = new Task();
        task.setTitle(""); // Invalid: blank title
        task.setStatus(TaskStatus.TODO);

        // Use ErrorResponse for validation errors
        ResponseEntity<ErrorResponse> response = rest.postForEntity(
            "/api/tasks", 
            task, 
            ErrorResponse.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void shouldFilterTasksByStatus() {
        repository.save(createTaskWithStatus(TaskStatus.TODO));
        repository.save(createTaskWithStatus(TaskStatus.DONE));

        ResponseEntity<Task[]> response = rest.getForEntity(
            "/api/tasks?status=TODO", 
            Task[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        for (Task task : response.getBody()) {
            assertEquals(TaskStatus.TODO, task.getStatus());
        }
    }

    // Helper methods
    private Task createSampleTask() {
        Task task = new Task();
        task.setTitle("Sample Task " + System.currentTimeMillis());
        task.setDescription("Test description");
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(LocalDate.now().plusDays(1));
        return task;
    }

    private Task createTaskWithStatus(TaskStatus status) {
        Task task = createSampleTask();
        task.setStatus(status);
        return repository.save(task);
    }
}