package com.example.taskManager.service;


import com.example.taskManager.enums.TaskStatus;
import com.example.taskManager.model.Task;
import com.example.taskManager.repository.TaskRepository;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }



    public Task getTaskById(Long id){
        log.info("get task by id");
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));

    }

    public List<Task> getAllTasks(){
        log.info("get all tasks");
        return taskRepository.findAll();
    }

    public Task createTask(Task task){
        if(task.getId() != null){
            throw new IllegalArgumentException("id should be empty");
        }
        if(task.getStatus() != null){
            throw new IllegalArgumentException("status should be empty");
        }

        var newTask = new Task(
                null,
                task.getCreatorId(),
                task.getAssignedUserId(),
                TaskStatus.CREATED,
                task.getCreateDateTime(),
                task.getDeadlineDate(),
                task.getPriority()
        );
        return taskRepository.save(newTask);

    }

    public Task updateTask(Long id, Task task){
        if(!taskRepository.existsById(id)){
            throw new NoSuchElementException();
        }
        if(task.getId() != null){
            throw new IllegalArgumentException();
        }
        if(task.getStatus() != null){
            throw new IllegalArgumentException();
        }

        var newTask = new Task(
                id,
                task.getCreatorId(),
                task.getAssignedUserId(),
                taskRepository.findById(id).get().getStatus(),
                task.getCreateDateTime(),
                task.getDeadlineDate(),
                task.getPriority()
        );

        return taskRepository.save(newTask);
    }

    public Task deleteTask(Long id){
        if(!taskRepository.existsById(id)){
            throw new NoSuchElementException();
        }
        Task task = taskRepository.findById(id).get();
        taskRepository.deleteById(id);
        return task;
    }

}
