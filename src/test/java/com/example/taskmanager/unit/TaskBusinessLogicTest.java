package com.example.taskmanager.unit;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Task entity business logic.
 * Tests the domain logic methods: isOverdue(), isDueToday(), equals(), hashCode(), toString()
 */
@DisplayName("Task Business Logic Tests")
class TaskBusinessLogicTest {

    // ==================== isOverdue() Tests ====================
    
    @Nested
    @DisplayName("isOverdue() Method Tests")
    class IsOverdueTests {

        @Test
        @DisplayName("Should return true when due date is in the past")
        void isOverdue_whenDueDateInPast_shouldReturnTrue() {
            Task task = new Task();
            task.setDueDate(LocalDate.now().minusDays(1));

            assertTrue(task.isOverdue(), "Task with past due date should be overdue");
        }

        @Test
        @DisplayName("Should return true when due date is multiple days in the past")
        void isOverdue_whenDueDateMultipleDaysInPast_shouldReturnTrue() {
            Task task = new Task();
            task.setDueDate(LocalDate.now().minusDays(30));

            assertTrue(task.isOverdue(), "Task with due date 30 days ago should be overdue");
        }

        @Test
        @DisplayName("Should return false when due date is today")
        void isOverdue_whenDueDateToday_shouldReturnFalse() {
            Task task = new Task();
            task.setDueDate(LocalDate.now());

            assertFalse(task.isOverdue(), "Task due today should not be overdue");
        }

        @Test
        @DisplayName("Should return false when due date is in the future")
        void isOverdue_whenDueDateInFuture_shouldReturnFalse() {
            Task task = new Task();
            task.setDueDate(LocalDate.now().plusDays(1));

            assertFalse(task.isOverdue(), "Task with future due date should not be overdue");
        }

        @Test
        @DisplayName("Should return false when due date is multiple days in the future")
        void isOverdue_whenDueDateMultipleDaysInFuture_shouldReturnFalse() {
            Task task = new Task();
            task.setDueDate(LocalDate.now().plusDays(365));

            assertFalse(task.isOverdue(), "Task with due date 1 year away should not be overdue");
        }

        @Test
        @DisplayName("Should return false when no due date is set")
        void isOverdue_whenNoDueDate_shouldReturnFalse() {
            Task task = new Task();
            task.setDueDate(null);

            assertFalse(task.isOverdue(), "Task without due date should not be overdue");
        }

        @Test
        @DisplayName("Should handle tasks with different statuses consistently")
        void isOverdue_shouldNotDependOnStatus() {
            LocalDate pastDate = LocalDate.now().minusDays(1);

            Task todoTask = new Task("Task", "Desc", TaskStatus.TODO, pastDate);
            Task inProgressTask = new Task("Task", "Desc", TaskStatus.IN_PROGRESS, pastDate);
            Task doneTask = new Task("Task", "Desc", TaskStatus.DONE, pastDate);

            assertTrue(todoTask.isOverdue());
            assertTrue(inProgressTask.isOverdue());
            assertTrue(doneTask.isOverdue(), "Even DONE tasks with past dates are considered overdue");
        }
    }

    // ==================== isDueToday() Tests ====================
    
    @Nested
    @DisplayName("isDueToday() Method Tests")
    class IsDueTodayTests {

        @Test
        @DisplayName("Should return true when due date is today")
        void isDueToday_whenDueDateToday_shouldReturnTrue() {
            Task task = new Task();
            task.setDueDate(LocalDate.now());

            assertTrue(task.isDueToday(), "Task due today should return true");
        }

        @Test
        @DisplayName("Should return false when due date is tomorrow")
        void isDueToday_whenDueDateTomorrow_shouldReturnFalse() {
            Task task = new Task();
            task.setDueDate(LocalDate.now().plusDays(1));

            assertFalse(task.isDueToday(), "Task due tomorrow should not be due today");
        }

        @Test
        @DisplayName("Should return false when due date is yesterday")
        void isDueToday_whenDueDateYesterday_shouldReturnFalse() {
            Task task = new Task();
            task.setDueDate(LocalDate.now().minusDays(1));

            assertFalse(task.isDueToday(), "Task due yesterday should not be due today");
        }

        @Test
        @DisplayName("Should return false when due date is in the future")
        void isDueToday_whenDueDateInFuture_shouldReturnFalse() {
            Task task = new Task();
            task.setDueDate(LocalDate.now().plusDays(7));

            assertFalse(task.isDueToday(), "Task due next week should not be due today");
        }

        @Test
        @DisplayName("Should return false when no due date is set")
        void isDueToday_whenNoDueDate_shouldReturnFalse() {
            Task task = new Task();
            task.setDueDate(null);

            assertFalse(task.isDueToday(), "Task without due date should not be due today");
        }

        @ParameterizedTest
        @EnumSource(TaskStatus.class)
        @DisplayName("Should work consistently across all task statuses")
        void isDueToday_shouldWorkForAllStatuses(TaskStatus status) {
            Task task = new Task("Task", "Desc", status, LocalDate.now());

            assertTrue(task.isDueToday(), 
                String.format("Task with status %s due today should return true", status));
        }
    }

    // ==================== equals() Tests ====================
    
    @Nested
    @DisplayName("equals() Method Tests")
    class EqualsTests {

        @Test
        @DisplayName("Should return true when comparing same instance")
        void equals_whenSameInstance_shouldReturnTrue() {
            Task task = new Task();
            task.setId(1L);

            assertEquals(task, task, "Task should equal itself");
        }

        @Test
        @DisplayName("Should return true when tasks have same non-null ID")
        void equals_whenSameId_shouldReturnTrue() {
            Task task1 = new Task();
            task1.setId(1L);
            task1.setTitle("Task 1");
            
            Task task2 = new Task();
            task2.setId(1L);
            task2.setTitle("Task 2");

            assertEquals(task1, task2, "Tasks with same ID should be equal");
        }

        @Test
        @DisplayName("Should return false when tasks have different IDs")
        void equals_whenDifferentId_shouldReturnFalse() {
            Task task1 = new Task();
            task1.setId(1L);
            
            Task task2 = new Task();
            task2.setId(2L);

            assertNotEquals(task1, task2, "Tasks with different IDs should not be equal");
        }

        @Test
        @DisplayName("Should return false when both IDs are null (transient entities)")
        void equals_whenBothIdsNull_shouldReturnFalse() {
            // Two transient entities (without IDs) are NOT considered equal
            // This is the standard JPA best practice for entity equality
            Task task1 = new Task();
            Task task2 = new Task();

            assertNotEquals(task1, task2, 
                "Two different transient entities should not be equal");
        }

        @Test
        @DisplayName("Should return false when one ID is null and other is not")
        void equals_whenOneIdNullOtherNot_shouldReturnFalse() {
            Task task1 = new Task();
            task1.setId(1L);
            
            Task task2 = new Task();
            task2.setId(null);

            assertNotEquals(task1, task2, 
                "Persisted and transient entities should not be equal");
        }

        @Test
        @DisplayName("Should return false when other ID is null and this is not")
        void equals_whenThisIdSetOtherNull_shouldReturnFalse() {
            Task task1 = new Task();
            task1.setId(null);
            
            Task task2 = new Task();
            task2.setId(1L);

            assertNotEquals(task1, task2, 
                "Transient and persisted entities should not be equal");
        }

        @Test
        @DisplayName("Should return false when compared to null")
        void equals_whenComparedToNull_shouldReturnFalse() {
            Task task = new Task();
            task.setId(1L);

            assertNotEquals(task, null, "Task should not equal null");
        }

        @Test
        @DisplayName("Should return false when compared to different class")
        void equals_whenComparedToDifferentClass_shouldReturnFalse() {
            Task task = new Task();
            task.setId(1L);

            assertNotEquals(task, "Not a Task", "Task should not equal String");
            assertNotEquals(task, 1L, "Task should not equal Long");
            assertNotEquals(task, new Object(), "Task should not equal Object");
        }

        @Test
        @DisplayName("Should ignore other fields when IDs match")
        void equals_shouldOnlyConsiderId_notOtherFields() {
            Task task1 = new Task("Title 1", "Desc 1", TaskStatus.TODO, LocalDate.now());
            task1.setId(1L);
            
            Task task2 = new Task("Title 2", "Desc 2", TaskStatus.DONE, LocalDate.now().plusDays(5));
            task2.setId(1L);

            assertEquals(task1, task2, 
                "Tasks with same ID should be equal regardless of other fields");
        }

        @Test
        @DisplayName("Should be symmetric: a.equals(b) == b.equals(a)")
        void equals_shouldBeSymmetric() {
            Task task1 = new Task();
            task1.setId(1L);
            
            Task task2 = new Task();
            task2.setId(1L);

            assertEquals(task1.equals(task2), task2.equals(task1),
                "equals() should be symmetric");
        }

        @Test
        @DisplayName("Should be transitive: if a=b and b=c then a=c")
        void equals_shouldBeTransitive() {
            Task task1 = new Task();
            task1.setId(1L);
            
            Task task2 = new Task();
            task2.setId(1L);
            
            Task task3 = new Task();
            task3.setId(1L);

            assertTrue(task1.equals(task2) && task2.equals(task3) && task1.equals(task3),
                "equals() should be transitive");
        }

        @Test
        @DisplayName("Should be consistent across multiple invocations")
        void equals_shouldBeConsistent() {
            Task task1 = new Task();
            task1.setId(1L);
            
            Task task2 = new Task();
            task2.setId(1L);

            boolean firstCall = task1.equals(task2);
            boolean secondCall = task1.equals(task2);
            boolean thirdCall = task1.equals(task2);

            assertEquals(firstCall, secondCall);
            assertEquals(secondCall, thirdCall);
        }
    }

    // ==================== hashCode() Tests ====================
    
    @Nested
    @DisplayName("hashCode() Method Tests")
    class HashCodeTests {

        @Test
        @DisplayName("Should return same hash code for tasks with same ID")
        void hashCode_whenSameId_shouldBeEqual() {
            Task task1 = new Task();
            task1.setId(1L);
            
            Task task2 = new Task();
            task2.setId(1L);

            assertEquals(task1.hashCode(), task2.hashCode(),
                "Tasks with same ID should have same hash code");
        }

        @Test
        @DisplayName("Should return consistent hash code across multiple calls")
        void hashCode_shouldBeConsistent() {
            Task task = new Task();
            task.setId(1L);

            int firstHash = task.hashCode();
            int secondHash = task.hashCode();
            int thirdHash = task.hashCode();

            assertEquals(firstHash, secondHash);
            assertEquals(secondHash, thirdHash);
        }

        @Test
        @DisplayName("Should maintain hash code contract with equals()")
        void hashCode_shouldMaintainContractWithEquals() {
            Task task1 = new Task("Title", "Desc", TaskStatus.TODO, LocalDate.now());
            task1.setId(1L);
            
            Task task2 = new Task("Different", "Also Different", TaskStatus.DONE, null);
            task2.setId(1L);

            if (task1.equals(task2)) {
                assertEquals(task1.hashCode(), task2.hashCode(),
                    "Equal objects must have equal hash codes");
            }
        }

        @Test
        @DisplayName("Should handle null ID gracefully")
        void hashCode_withNullId_shouldNotThrowException() {
            Task task = new Task();
            task.setId(null);

            assertDoesNotThrow(() -> task.hashCode(),
                "hashCode() should handle null ID without exception");
        }

        @Test
        @DisplayName("Different IDs may produce different hash codes")
        void hashCode_withDifferentIds_mayProduceDifferentHashes() {
            Task task1 = new Task();
            task1.setId(1L);
            
            Task task2 = new Task();
            task2.setId(2L);

            // Note: This is not required by the contract, but is good practice
            // Different objects CAN have the same hash code (collisions)
            int hash1 = task1.hashCode();
            int hash2 = task2.hashCode();

            // We just verify they don't throw exceptions
            assertNotNull(hash1);
            assertNotNull(hash2);
        }
    }

    // ==================== toString() Tests ====================
    
    @Nested
    @DisplayName("toString() Method Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all key fields")
        void toString_shouldContainKeyFields() {
            Task task = new Task();
            task.setId(1L);
            task.setTitle("Test Task");
            task.setStatus(TaskStatus.TODO);
            task.setDueDate(LocalDate.of(2024, 12, 31));

            String result = task.toString();

            assertAll("toString should contain all key fields",
                () -> assertTrue(result.contains("id=1"), "Should contain ID"),
                () -> assertTrue(result.contains("title='Test Task'"), "Should contain title"),
                () -> assertTrue(result.contains("status=TODO"), "Should contain status"),
                () -> assertTrue(result.contains("dueDate=2024-12-31"), "Should contain due date")
            );
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void toString_withNullValues_shouldNotThrowException() {
            Task task = new Task();
            task.setId(null);
            task.setTitle(null);
            task.setDueDate(null);

            assertDoesNotThrow(() -> task.toString(),
                "toString() should handle null values");
        }

        @Test
        @DisplayName("Should return non-null, non-empty string")
        void toString_shouldReturnNonNullNonEmptyString() {
            Task task = new Task();
            task.setId(1L);
            task.setTitle("Task");

            String result = task.toString();

            assertNotNull(result, "toString() should not return null");
            assertFalse(result.isEmpty(), "toString() should not return empty string");
        }

        @Test
        @DisplayName("Should be consistent across multiple calls")
        void toString_shouldBeConsistent() {
            Task task = new Task("Task", "Description", TaskStatus.IN_PROGRESS, LocalDate.now());
            task.setId(42L);

            String first = task.toString();
            String second = task.toString();

            assertEquals(first, second, "Multiple toString() calls should produce same result");
        }
    }

    // ==================== Constructor Tests ====================
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("No-arg constructor should create task with default values")
        void noArgConstructor_shouldCreateTaskWithDefaults() {
            Task task = new Task();

            assertNull(task.getId(), "ID should be null for new task");
            assertNull(task.getTitle(), "Title should be null");
            assertNull(task.getDescription(), "Description should be null");
            assertNull(task.getDueDate(), "Due date should be null");
        }

        @Test
        @DisplayName("Parameterized constructor should set all fields")
        void constructor_withParameters_shouldSetAllFields() {
            LocalDate dueDate = LocalDate.now().plusDays(5);
            Task task = new Task("Title", "Description", TaskStatus.IN_PROGRESS, dueDate);

            assertAll("All constructor parameters should be set",
                () -> assertEquals("Title", task.getTitle()),
                () -> assertEquals("Description", task.getDescription()),
                () -> assertEquals(TaskStatus.IN_PROGRESS, task.getStatus()),
                () -> assertEquals(dueDate, task.getDueDate())
            );
        }

        @Test
        @DisplayName("Constructor with null status should default to TODO")
        void constructor_withNullStatus_shouldDefaultToTODO() {
            Task task = new Task("Title", "Description", null, null);

            assertEquals(TaskStatus.TODO, task.getStatus(),
                "Null status should default to TODO");
        }

        @Test
        @DisplayName("Constructor should accept null title")
        void constructor_shouldAcceptNullTitle() {
            assertDoesNotThrow(() -> new Task(null, "Description", TaskStatus.TODO, null),
                "Constructor should accept null title");
        }

        @Test
        @DisplayName("Constructor should accept null description")
        void constructor_shouldAcceptNullDescription() {
            Task task = new Task("Title", null, TaskStatus.TODO, null);

            assertNull(task.getDescription(), "Description can be null");
        }

        @Test
        @DisplayName("Constructor should accept null due date")
        void constructor_shouldAcceptNullDueDate() {
            Task task = new Task("Title", "Description", TaskStatus.TODO, null);

            assertNull(task.getDueDate(), "Due date can be null");
        }
    }

    // ==================== Edge Cases and Integration Tests ====================
    
    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Task should maintain state consistency after multiple operations")
        void task_shouldMaintainStateConsistency() {
            Task task = new Task("Original", "Description", TaskStatus.TODO, LocalDate.now());
            task.setId(1L);

            // Perform multiple operations
            task.setTitle("Updated");
            task.setStatus(TaskStatus.IN_PROGRESS);
            task.setDueDate(LocalDate.now().plusDays(1));

            assertAll("Task state should be consistent",
                () -> assertEquals(1L, task.getId()),
                () -> assertEquals("Updated", task.getTitle()),
                () -> assertEquals(TaskStatus.IN_PROGRESS, task.getStatus()),
                () -> assertFalse(task.isOverdue()),
                () -> assertFalse(task.isDueToday())
            );
        }

        @Test
        @DisplayName("Business logic methods should work correctly after deserialization scenario")
        void businessLogic_shouldWorkAfterStateChanges() {
            // Simulate a task that goes through various state changes
            Task task = new Task();
            task.setId(1L);
            task.setTitle("Task");
            task.setStatus(TaskStatus.TODO);
            task.setDueDate(LocalDate.now().minusDays(1));

            assertTrue(task.isOverdue(), "Should be overdue");

            // Update due date
            task.setDueDate(LocalDate.now());
            assertFalse(task.isOverdue(), "Should not be overdue");
            assertTrue(task.isDueToday(), "Should be due today");

            // Update due date again
            task.setDueDate(LocalDate.now().plusDays(1));
            assertFalse(task.isOverdue(), "Should not be overdue");
            assertFalse(task.isDueToday(), "Should not be due today");
        }

        @Test
        @DisplayName("Equals and hashCode should work in collections")
        void equalsAndHashCode_shouldWorkInCollections() {
            Task task1 = new Task();
            task1.setId(1L);
            
            Task task2 = new Task();
            task2.setId(1L);
            
            Task task3 = new Task();
            task3.setId(2L);

            java.util.Set<Task> taskSet = new java.util.HashSet<>();
            taskSet.add(task1);
            taskSet.add(task2); // Should not be added (same ID as task1)
            taskSet.add(task3);

            assertEquals(2, taskSet.size(), 
                "Set should contain only 2 tasks (task1 and task3)");
            assertTrue(taskSet.contains(task1));
            assertTrue(taskSet.contains(task2)); // Should find task2 via task1
            assertTrue(taskSet.contains(task3));
        }
    }
}