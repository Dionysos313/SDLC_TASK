package com.example.taskmanager.unit;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.TaskMapper;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    private TaskMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TaskMapper();
    }

    @Test
    void toDTO_whenTaskIsNull_shouldReturnNull() {
        TaskDTO result = mapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void toDTO_whenTaskHasAllFields_shouldMapAllFields() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDueDate(LocalDate.of(2024, 12, 31));
        task.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        task.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 10, 0));

        TaskDTO dto = mapper.toDTO(task);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Test Task", dto.getTitle());
        assertEquals("Description", dto.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, dto.getStatus());
        assertEquals(LocalDate.of(2024, 12, 31), dto.getDueDate());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 1, 2, 10, 0), dto.getUpdatedAt());
    }

    @Test
    void toDTO_whenTaskHasNullFields_shouldHandleNulls() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test");

        TaskDTO dto = mapper.toDTO(task);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Test", dto.getTitle());
        assertNull(dto.getDescription());
        assertNull(dto.getDueDate());
    }

    @Test
    void toEntity_whenDTOIsNull_shouldReturnNull() {
        Task result = mapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_whenDTOHasAllFields_shouldMapAllFields() {
        TaskDTO dto = new TaskDTO();
        dto.setId(1L);
        dto.setTitle("Test Task");
        dto.setDescription("Description");
        dto.setStatus(TaskStatus.DONE);
        dto.setDueDate(LocalDate.of(2024, 12, 31));

        Task task = mapper.toEntity(dto);

        assertNotNull(task);
        assertEquals(1L, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Description", task.getDescription());
        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals(LocalDate.of(2024, 12, 31), task.getDueDate());
    }

    @Test
    void toEntity_shouldNotMapTimestamps() {
        TaskDTO dto = new TaskDTO();
        dto.setId(1L);
        dto.setTitle("Test");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        Task task = mapper.toEntity(dto);

        assertNotNull(task);
        // Timestamps should be null because they're managed by JPA
        assertNull(task.getCreatedAt());
        assertNull(task.getUpdatedAt());
    }
}   