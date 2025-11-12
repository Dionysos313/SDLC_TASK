package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:5173","http://localhost:5174","https://your-vercel-project.vercel.app","https://neat-seas-wonder.loca.lt"})
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskRepository repo;

  public TaskController(TaskRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Task> getAll() {
    return repo.findAll();
  }

  @GetMapping("{id}")
  public ResponseEntity<Task> getById(@PathVariable Long id) {
    Optional<Task> opt = repo.findById(id);
    return opt.map(ResponseEntity::ok)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
  }

  @PostMapping
  public ResponseEntity<Task> create(@Valid @RequestBody Task task) {
    task.setId(null);
    Task saved = repo.save(task);
    return new ResponseEntity<>(saved, HttpStatus.CREATED);
  }

  @PutMapping("{id}")
  public ResponseEntity<Task> update(@PathVariable Long id, @Valid @RequestBody Task task) {
    return repo.findById(id).map(existing -> {
      existing.setTitle(task.getTitle());
      existing.setDescription(task.getDescription());
      existing.setStatus(task.getStatus());
      existing.setDueDate(task.getDueDate());
      Task saved = repo.save(existing);
      return ResponseEntity.ok(saved);
    }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
  }

  @DeleteMapping("{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    return repo.findById(id).map(t -> {
      repo.delete(t);
      return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
  }
}