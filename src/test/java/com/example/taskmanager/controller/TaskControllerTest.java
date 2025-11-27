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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TaskController using MockMvc.
 */
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTasks_shouldReturnAllTasks() throws Exception {
        Task task1 = createTask(1L, "Task 1", TaskStatus.TODO);
        Task task2 = createTask(2L, "Task 2", TaskStatus.IN_PROGRESS);
        List<Task> tasks = Arrays.asList(task1, task2);

        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].title", is("Task 2")));

        verify(taskService).getAllTasks();
    }

    @Test
    void getAllTasks_withStatusFilter_shouldReturnFilteredTasks() throws Exception {
        Task task = createTask(1L, "TODO Task", TaskStatus.TODO);
        when(taskService.getTasksByStatus(TaskStatus.TODO)).thenReturn(Arrays.asList(task));

        mockMvc.perform(get("/api/tasks")
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("TODO")));

        verify(taskService).getTasksByStatus(TaskStatus.TODO);
        verify(taskService, never()).getAllTasks();
    }

    @Test
    void getById_whenTaskExists_shouldReturnTask() throws Exception {
        Task task = createTask(1L, "Test Task", TaskStatus.TODO);
        when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService).getTaskById(1L);
    }

    @Test
    void getById_whenTaskNotFound_shouldReturn404() throws Exception {
        when(taskService.getTaskById(999L)).thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        verify(taskService).getTaskById(999L);
    }

    @Test
    void getOverdueTasks_shouldReturnOverdueTasks() throws Exception {
        Task overdueTask = createTask(1L, "Overdue", TaskStatus.TODO);
        overdueTask.setDueDate(LocalDate.now().minusDays(1));
        
        when(taskService.getOverdueTasks()).thenReturn(Arrays.asList(overdueTask));

        mockMvc.perform(get("/api/tasks/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Overdue")));

        verify(taskService).getOverdueTasks();
    }

    @Test
    void create_withValidTask_shouldReturnCreated() throws Exception {
        Task newTask = new Task("New Task", "Description", TaskStatus.TODO, null);
        Task savedTask = createTask(1L, "New Task", TaskStatus.TODO);
        
        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("New Task")));

        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void create_withBlankTitle_shouldReturn400() throws Exception {
        Task invalidTask = new Task("", "Description", TaskStatus.TODO, null);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));

        verify(taskService, never()).createTask(any());
    }

    @Test
    void create_withTitleTooLong_shouldReturn400() throws Exception {
        Task invalidTask = new Task("A".repeat(101), "Description", TaskStatus.TODO, null);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    void update_withValidTask_shouldReturnUpdatedTask() throws Exception {
        Task updatedTask = createTask(1L, "Updated Task", TaskStatus.IN_PROGRESS);
        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Task")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        verify(taskService).updateTask(eq(1L), any(Task.class));
    }

    @Test
    void update_whenTaskNotFound_shouldReturn404() throws Exception {
        Task task = new Task("Updated", "Desc", TaskStatus.TODO, null);
        when(taskService.updateTask(eq(999L), any(Task.class)))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(put("/api/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isNotFound());

        verify(taskService).updateTask(eq(999L), any(Task.class));
    }

    @Test
    void updateTaskStatus_shouldUpdateStatusOnly() throws Exception {
        Task updatedTask = createTask(1L, "Task", TaskStatus.DONE);
        when(taskService.updateTaskStatus(1L, TaskStatus.DONE)).thenReturn(updatedTask);

        mockMvc.perform(patch("/api/tasks/1/status")
                .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DONE")));

        verify(taskService).updateTaskStatus(1L, TaskStatus.DONE);
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void delete_whenTaskNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Task not found"))
                .when(taskService).deleteTask(999L);

        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskService).deleteTask(999L);
    }

    // Helper method
    private Task createTask(Long id, String title, TaskStatus status) {
        Task task = new Task(title, "Description", status, LocalDate.now().plusDays(1));
        task.setId(id);
        return task;
    }
}