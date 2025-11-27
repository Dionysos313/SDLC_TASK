package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for Task business logic.
 * Separates business rules from controller and repository concerns.
 */
@Service
@Transactional
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        log.debug("Fetching all tasks");
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        log.debug("Fetching task with id: {}", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(TaskStatus status) {
        log.debug("Fetching tasks with status: {}", status);
        return taskRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Task> getOverdueTasks() {
        log.debug("Fetching overdue tasks");
        return taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE);
    }

    public Task createTask(Task task) {
        log.info("Creating new task: {}", task.getTitle());
        // Ensure ID is null for new entities
        task.setId(null);
        
        // Set default status if not provided
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task taskDetails) {
        log.info("Updating task with id: {}", id);
        
        Task existingTask = getTaskById(id);
        
        // Update only provided fields
        if (taskDetails.getTitle() != null) {
            existingTask.setTitle(taskDetails.getTitle());
        }
        if (taskDetails.getDescription() != null) {
            existingTask.setDescription(taskDetails.getDescription());
        }
        if (taskDetails.getStatus() != null) {
            existingTask.setStatus(taskDetails.getStatus());
        }
        if (taskDetails.getDueDate() != null) {
            existingTask.setDueDate(taskDetails.getDueDate());
        }
        
        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

    public Task updateTaskStatus(Long id, TaskStatus newStatus) {
        log.info("Updating task {} status to {}", id, newStatus);
        Task task = getTaskById(id);
        task.setStatus(newStatus);
        return taskRepository.save(task);
    }
}