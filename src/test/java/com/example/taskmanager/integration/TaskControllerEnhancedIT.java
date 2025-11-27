package com.example.taskmanager.integration;

import com.example.taskmanager.exception.ErrorResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerEnhancedIT {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private TaskRepository repository;

    @LocalServerPort
    private int port;

    private RestTemplate patchRestTemplate;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        // Create a RestTemplate that supports PATCH
        patchRestTemplate = new RestTemplate();
        patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    void createTask_withMinimalData_shouldSucceed() {
        Task task = new Task();
        task.setTitle("Minimal Task");

        ResponseEntity<Task> response = rest.postForEntity("/api/tasks", task, Task.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        // Verify in database
        var tasks = repository.findAll();
        assertFalse(tasks.isEmpty());
        assertEquals("Minimal Task", tasks.get(0).getTitle());
        assertEquals(TaskStatus.TODO, tasks.get(0).getStatus()); // Default
    }

    @Test
    void createTask_withTitleTooLong_shouldFail() {
        Task task = new Task();
        task.setTitle("A".repeat(101)); // Exceeds max length

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
            "/api/tasks",
            task,
            ErrorResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getValidationErrors());
    }

    @Test
    void createTask_withDescriptionTooLong_shouldFail() {
        Task task = new Task();
        task.setTitle("Valid Title");
        task.setDescription("D".repeat(501)); // Exceeds max length

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
            "/api/tasks",
            task,
            ErrorResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateTask_withPartialData_shouldUpdateOnlyProvidedFields() {
        // Create initial task
        Task original = new Task();
        original.setTitle("Original Title");
        original.setDescription("Original Description");
        original.setStatus(TaskStatus.TODO);
        original.setDueDate(LocalDate.now().plusDays(5));
        original = repository.save(original);

        // Update only title
        Task update = new Task();
        update.setTitle("Updated Title");

        HttpEntity<Task> requestEntity = new HttpEntity<>(update);
        ResponseEntity<Task> response = rest.exchange(
            "/api/tasks/" + original.getId(),
            HttpMethod.PUT,
            requestEntity,
            Task.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify only title changed
        Task updated = repository.findById(original.getId()).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Original Description", updated.getDescription());
    }

    @Test
    void updateTaskStatus_shouldUpdateStatusOnly() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.TODO);
        task = repository.save(task);

        // Use patchRestTemplate that supports PATCH
        String url = "http://localhost:" + port + "/api/tasks/" + task.getId() + "/status?status=IN_PROGRESS";
        
        ResponseEntity<Task> response = patchRestTemplate.exchange(
            url,
            HttpMethod.PATCH,
            null,
            Task.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TaskStatus.IN_PROGRESS, response.getBody().getStatus());
        
        // Verify in database
        Task updated = repository.findById(task.getId()).orElseThrow();
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void getOverdueTasks_shouldReturnOnlyOverdueTasks() {
        // Create overdue task
        Task overdue = new Task();
        overdue.setTitle("Overdue Task");
        overdue.setStatus(TaskStatus.TODO);
        overdue.setDueDate(LocalDate.now().minusDays(1));
        repository.save(overdue);

        // Create future task
        Task future = new Task();
        future.setTitle("Future Task");
        future.setStatus(TaskStatus.TODO);
        future.setDueDate(LocalDate.now().plusDays(1));
        repository.save(future);

        // Create done task (even if overdue, should not be included)
        Task done = new Task();
        done.setTitle("Done Task");
        done.setStatus(TaskStatus.DONE);
        done.setDueDate(LocalDate.now().minusDays(2));
        repository.save(done);

        ResponseEntity<Task[]> response = rest.getForEntity("/api/tasks/overdue", Task[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("Overdue Task", response.getBody()[0].getTitle());
    }

    @Test
    void filterByStatus_withMultipleStatuses_shouldReturnCorrectTasks() {
        // Create tasks with different statuses
        Task todo = createTaskWithStatus(TaskStatus.TODO);
        Task inProgress = createTaskWithStatus(TaskStatus.IN_PROGRESS);
        Task done = createTaskWithStatus(TaskStatus.DONE);

        repository.save(todo);
        repository.save(inProgress);
        repository.save(done);

        // Test TODO filter
        ResponseEntity<Task[]> todoResponse = rest.getForEntity(
            "/api/tasks?status=TODO",
            Task[].class
        );
        assertEquals(1, todoResponse.getBody().length);
        assertEquals(TaskStatus.TODO, todoResponse.getBody()[0].getStatus());

        // Test IN_PROGRESS filter
        ResponseEntity<Task[]> inProgressResponse = rest.getForEntity(
            "/api/tasks?status=IN_PROGRESS",
            Task[].class
        );
        assertEquals(1, inProgressResponse.getBody().length);
        assertEquals(TaskStatus.IN_PROGRESS, inProgressResponse.getBody()[0].getStatus());

        // Test DONE filter
        ResponseEntity<Task[]> doneResponse = rest.getForEntity(
            "/api/tasks?status=DONE",
            Task[].class
        );
        assertEquals(1, doneResponse.getBody().length);
        assertEquals(TaskStatus.DONE, doneResponse.getBody()[0].getStatus());
    }

    @Test
    void deleteNonExistentTask_shouldReturn404() {
        ResponseEntity<ErrorResponse> response = rest.exchange(
            "/api/tasks/9999",
            HttpMethod.DELETE,
            null,
            ErrorResponse.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateNonExistentTask_shouldReturn404() {
        Task task = new Task();
        task.setTitle("Updated");

        HttpEntity<Task> requestEntity = new HttpEntity<>(task);
        ResponseEntity<ErrorResponse> response = rest.exchange(
            "/api/tasks/9999",
            HttpMethod.PUT,
            requestEntity,
            ErrorResponse.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createTask_withNullTitle_shouldFail() {
        Task task = new Task();
        task.setDescription("Description without title");

        ResponseEntity<ErrorResponse> response = rest.postForEntity(
            "/api/tasks",
            task,
            ErrorResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Helper methods
    private Task createTaskWithStatus(TaskStatus status) {
        Task task = new Task();
        task.setTitle("Task with status " + status);
        task.setStatus(status);
        task.setDueDate(LocalDate.now().plusDays(1));
        return task;
    }
}