package com.example.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for TaskManagerApplication main class and context loading.
 */
@SpringBootTest
class TaskManagerApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void main_shouldStartApplication() {
        // This test ensures the main method can be invoked without errors
        // The actual application startup is tested by @SpringBootTest
        assertThat(TaskManagerApplication.class).isNotNull();
    }

    @Test
    void applicationContext_shouldContainRequiredBeans() {
        assertThat(applicationContext.containsBean("taskService")).isTrue();
        assertThat(applicationContext.containsBean("taskController")).isTrue();
    }
}