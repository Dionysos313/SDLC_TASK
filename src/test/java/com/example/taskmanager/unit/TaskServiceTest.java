package com.example.taskmanager.unit;

import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Test Task");
        sampleTask.setDescription("Description");
        sampleTask.setStatus(TaskStatus.TODO);
        sampleTask.setDueDate(LocalDate.now().plusDays(5));
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        when(taskRepository.findAll()).thenReturn(Arrays.asList(sampleTask));

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTaskById_whenExists_shouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Task task = taskService.getTaskById(1L);

        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_whenNotExists_shouldThrowException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.getTaskById(999L)
        );

        assertTrue(exception.getMessage().contains("not found with id: 999"));
        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    void getTasksByStatus_shouldReturnFilteredTasks() {
        when(taskRepository.findByStatus(TaskStatus.TODO))
            .thenReturn(Arrays.asList(sampleTask));

        List<Task> tasks = taskService.getTasksByStatus(TaskStatus.TODO);

        assertEquals(1, tasks.size());
        assertEquals(TaskStatus.TODO, tasks.get(0).getStatus());
        verify(taskRepository, times(1)).findByStatus(TaskStatus.TODO);
    }

    @Test
    void getOverdueTasks_shouldReturnOverdueTasks() {
        Task overdueTask = new Task();
        overdueTask.setTitle("Overdue");
        overdueTask.setDueDate(LocalDate.now().minusDays(1));
        overdueTask.setStatus(TaskStatus.TODO);

        when(taskRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), eq(TaskStatus.DONE)))
            .thenReturn(Arrays.asList(overdueTask));

        List<Task> overdue = taskService.getOverdueTasks();

        assertEquals(1, overdue.size());
        verify(taskRepository, times(1))
            .findByDueDateBeforeAndStatusNot(any(LocalDate.class), eq(TaskStatus.DONE));
    }

    @Test
    void createTask_shouldSetDefaultStatus() {
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setStatus(null);

        when(taskRepository.save(any(Task.class))).thenReturn(newTask);

        Task created = taskService.createTask(newTask);

        assertEquals(TaskStatus.TODO, newTask.getStatus());
        assertNull(newTask.getId());
        verify(taskRepository, times(1)).save(newTask);
    }

    @Test
    void createTask_shouldNullifyId() {
        Task newTask = new Task();
        newTask.setId(999L); // Should be ignored
        newTask.setTitle("New Task");
        newTask.setStatus(TaskStatus.TODO);

        when(taskRepository.save(any(Task.class))).thenReturn(newTask);

        taskService.createTask(newTask);

        assertNull(newTask.getId());
        verify(taskRepository, times(1)).save(newTask);
    }

    @Test
    void updateTask_shouldUpdateAllFields() {
        Task updates = new Task();
        updates.setTitle("Updated Title");
        updates.setDescription("Updated Description");
        updates.setStatus(TaskStatus.DONE);
        updates.setDueDate(LocalDate.now().plusDays(10));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        Task updated = taskService.updateTask(1L, updates);

        assertEquals("Updated Title", sampleTask.getTitle());
        assertEquals("Updated Description", sampleTask.getDescription());
        assertEquals(TaskStatus.DONE, sampleTask.getStatus());
        assertNotNull(sampleTask.getDueDate());
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    void updateTask_shouldOnlyUpdateProvidedFields() {
        Task updates = new Task();
        updates.setTitle("Updated Title");
        // Other fields are null

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        taskService.updateTask(1L, updates);

        assertEquals("Updated Title", sampleTask.getTitle());
        assertEquals("Description", sampleTask.getDescription()); // Unchanged
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    void updateTask_whenNotExists_shouldThrowException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.updateTask(999L, new Task())
        );
    }

    @Test
    void deleteTask_whenExists_shouldDelete() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        doNothing().when(taskRepository).delete(sampleTask);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).delete(sampleTask);
    }

    @Test
    void deleteTask_whenNotExists_shouldThrowException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.deleteTask(999L)
        );
    }

    @Test
    void updateTaskStatus_shouldUpdateStatusOnly() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        Task updated = taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, sampleTask.getStatus());
        assertEquals("Test Task", sampleTask.getTitle()); // Unchanged
        verify(taskRepository, times(1)).save(sampleTask);
    }
}