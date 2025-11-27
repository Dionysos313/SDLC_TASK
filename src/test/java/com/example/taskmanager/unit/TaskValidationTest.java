package com.example.taskmanager.unit;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import jakarta.validation.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TaskValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setupValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void titleIsRequired() {
    Task t = new Task();
    t.setTitle(""); // blank
    t.setStatus(TaskStatus.TODO);
    Set<ConstraintViolation<Task>> violations = validator.validate(t);
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
  }

  @Test
  void titleMaxLength() {
    Task t = new Task();
    t.setTitle("x".repeat(200));
    Set<ConstraintViolation<Task>> violations = validator.validate(t);
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
  }

  @Test
  void validTaskHasNoViolations() {
    Task t = new Task();
    t.setTitle("Sample");
    t.setDescription("desc");
    t.setStatus(TaskStatus.TODO);
    t.setDueDate(LocalDate.now().plusDays(1));
    Set<ConstraintViolation<Task>> violations = validator.validate(t);
    assertTrue(violations.isEmpty());
  }
}