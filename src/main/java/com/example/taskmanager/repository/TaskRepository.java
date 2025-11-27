package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Task entity.
 * Provides custom query methods using Spring Data JPA conventions.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks with a specific status.
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Find tasks due before a certain date and not in DONE status.
     */
    List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    /**
     * Find tasks due on a specific date.
     */
    List<Task> findByDueDate(LocalDate dueDate);

    /**
     * Find tasks by title containing search term (case-insensitive).
     */
    List<Task> findByTitleContainingIgnoreCase(String searchTerm);

    /**
     * Find all tasks ordered by due date (null dates last).
     */
    @Query("SELECT t FROM Task t ORDER BY CASE WHEN t.dueDate IS NULL THEN 1 ELSE 0 END, t.dueDate ASC")
    List<Task> findAllOrderedByDueDate();

    /**
     * Count tasks by status.
     */
    long countByStatus(TaskStatus status);

    /**
     * Check if a task with the given title exists (case-insensitive).
     */
    boolean existsByTitleIgnoreCase(String title);
}