package com.example.taskManager.controller;

import com.example.taskManager.dto.TaskDto.TaskCreateRequestDto;
import com.example.taskManager.dto.TaskDto.TaskResponseDto;
import com.example.taskManager.dto.TaskDto.TaskUpdateRequestDto;
import com.example.taskManager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.status(200).body(taskService.getTaskById(id, email));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(
            Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(taskService.getAllTasks(email));
    }


    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
           @RequestBody @Valid TaskCreateRequestDto task,
           Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(201).body(taskService.createTask(task, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @RequestBody @Valid TaskUpdateRequestDto request,
            Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(taskService.updateTask(id, request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponseDto> deleteTask(
            @PathVariable Long id,
            Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(taskService.deleteTask(id, email));
    }





}
