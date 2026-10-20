package com.example.taskManager.service;


import com.example.taskManager.enums.TaskPriority;
import com.example.taskManager.enums.TaskStatus;
import com.example.taskManager.model.Task;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final Map<Long, Task> taskMap;

    private final AtomicLong idCounter;

    public TaskService(){
        taskMap = new ConcurrentHashMap<>();
        idCounter = new AtomicLong();
    }



    public Task getTaskById(Long id){
        if(!taskMap.containsKey(id)){
            throw new NoSuchElementException("this task doesnt contain: " + id);
        }
        log.info("get task by id");
        return taskMap.get(id);
    }

    public List<Task> getAllTasks(){
        log.info("get all tasks");
        return taskMap.values().stream().toList();
    }

    public Task createTask(Task task){
        if(task.getId() != null){
            throw new IllegalArgumentException("id should be empty");
        }
        if(task.getStatus() != null){
            throw new IllegalArgumentException("status should be empty");
        }

        var newTask = new Task(
                idCounter.incrementAndGet(),
                task.getCreatorId(),
                task.getAssignedUserId(),
                TaskStatus.CREATED,
                task.getCreateDateTime(),
                task.getDeadlineDate(),
                task.getPriority()
        );
        taskMap.put(newTask.getId(),  newTask);
        return newTask;

    }

    public Task updateTask(Long id, Task task){
        if(!taskMap.containsKey(id)){
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
                taskMap.get(id).getStatus(),
                task.getCreateDateTime(),
                task.getDeadlineDate(),
                task.getPriority()
        );

        taskMap.put(id, newTask);

        return newTask;
    }

    public Task deleteTask(Long id){
        if(!taskMap.containsKey(id)){
            throw new NoSuchElementException();
        }
        var task = taskMap.get(id);
        taskMap.remove(id);

        return task;

    }

}
