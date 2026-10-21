package com.example.taskManager.controller;

import com.example.taskManager.dto.TaskRequestDto;
import com.example.taskManager.dto.TaskResponseDto;
import com.example.taskManager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.status(200).body(taskService.getTaskById(id));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(){
        return ResponseEntity.status(200).body(taskService.getAllTasks());
    }


    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
           @RequestBody TaskRequestDto task
    ){
        return ResponseEntity.status(201).body(taskService.createTask(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequestDto task
    ){
        return ResponseEntity.status(200).body(taskService.updateTask(id, task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponseDto> deleteTask(
            @PathVariable Long id
    ){
        return ResponseEntity.status(200).body(taskService.deleteTask(id));
    }





}
