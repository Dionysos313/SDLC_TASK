package com.example.taskmanager.unit;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void findByStatus_shouldReturnTasksWithMatchingStatus() {
        Task todo = createTask("TODO Task", TaskStatus.TODO);
        Task done = createTask("DONE Task", TaskStatus.DONE);
        
        repository.save(todo);
        repository.save(done);

        List<Task> todoTasks = repository.findByStatus(TaskStatus.TODO);

        assertEquals(1, todoTasks.size());
        assertEquals("TODO Task", todoTasks.get(0).getTitle());
    }

    @Test
    void findByDueDateBeforeAndStatusNot_shouldReturnOverdueTasks() {
        Task overdueTodo = createTask("Overdue TODO", TaskStatus.TODO);
        overdueTodo.setDueDate(LocalDate.now().minusDays(1));
        
        Task overdueDone = createTask("Overdue DONE", TaskStatus.DONE);
        overdueDone.setDueDate(LocalDate.now().minusDays(1));
        
        Task futureTodo = createTask("Future TODO", TaskStatus.TODO);
        futureTodo.setDueDate(LocalDate.now().plusDays(1));

        repository.save(overdueTodo);
        repository.save(overdueDone);
        repository.save(futureTodo);

        List<Task> overdue = repository.findByDueDateBeforeAndStatusNot(
            LocalDate.now(),
            TaskStatus.DONE
        );

        assertEquals(1, overdue.size());
        assertEquals("Overdue TODO", overdue.get(0).getTitle());
    }

    @Test
    void findByDueDate_shouldReturnTasksWithExactDate() {
        LocalDate targetDate = LocalDate.of(2024, 12, 31);
        
        Task task1 = createTask("Task 1", TaskStatus.TODO);
        task1.setDueDate(targetDate);
        
        Task task2 = createTask("Task 2", TaskStatus.TODO);
        task2.setDueDate(LocalDate.of(2024, 12, 30));

        repository.save(task1);
        repository.save(task2);

        List<Task> tasks = repository.findByDueDate(targetDate);

        assertEquals(1, tasks.size());
        assertEquals("Task 1", tasks.get(0).getTitle());
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnMatchingTasks() {
        Task task1 = createTask("Important Task", TaskStatus.TODO);
        Task task2 = createTask("Another IMPORTANT item", TaskStatus.TODO);
        Task task3 = createTask("Different task", TaskStatus.TODO);

        repository.save(task1);
        repository.save(task2);
        repository.save(task3);

        List<Task> results = repository.findByTitleContainingIgnoreCase("important");

        assertEquals(2, results.size());
    }

    @Test
    void findAllOrderedByDueDate_shouldOrderCorrectly() {
        Task task1 = createTask("Future", TaskStatus.TODO);
        task1.setDueDate(LocalDate.now().plusDays(2));
        
        Task task2 = createTask("Tomorrow", TaskStatus.TODO);
        task2.setDueDate(LocalDate.now().plusDays(1));
        
        Task task3 = createTask("No Date", TaskStatus.TODO);
        task3.setDueDate(null);

        repository.save(task1);
        repository.save(task2);
        repository.save(task3);

        List<Task> ordered = repository.findAllOrderedByDueDate();

        assertEquals(3, ordered.size());
        assertEquals("Tomorrow", ordered.get(0).getTitle());
        assertEquals("Future", ordered.get(1).getTitle());
        assertEquals("No Date", ordered.get(2).getTitle());
    }

    @Test
    void countByStatus_shouldReturnCorrectCount() {
        repository.save(createTask("TODO 1", TaskStatus.TODO));
        repository.save(createTask("TODO 2", TaskStatus.TODO));
        repository.save(createTask("DONE 1", TaskStatus.DONE));

        long todoCount = repository.countByStatus(TaskStatus.TODO);
        long doneCount = repository.countByStatus(TaskStatus.DONE);
        long inProgressCount = repository.countByStatus(TaskStatus.IN_PROGRESS);

        assertEquals(2, todoCount);
        assertEquals(1, doneCount);
        assertEquals(0, inProgressCount);
    }

    @Test
    void existsByTitleIgnoreCase_shouldReturnTrueWhenExists() {
        Task task = createTask("Unique Task", TaskStatus.TODO);
        repository.save(task);

        assertTrue(repository.existsByTitleIgnoreCase("Unique Task"));
        assertTrue(repository.existsByTitleIgnoreCase("UNIQUE TASK"));
        assertTrue(repository.existsByTitleIgnoreCase("unique task"));
        assertFalse(repository.existsByTitleIgnoreCase("Non Existent"));
    }

    private Task createTask(String title, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(status);
        return task;
    }
}