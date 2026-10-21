package com.example.taskManager.service;

import com.example.taskManager.dto.TaskRequestDto;
import com.example.taskManager.dto.TaskResponseDto;
import com.example.taskManager.enums.TaskStatus;
import com.example.taskManager.model.Task;
import com.example.taskManager.repository.TaskRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }


    //Получение задач из бд по ID
    public TaskResponseDto getTaskById(Long id){
        log.info("get task by id");
        return toResponse(taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id)));

    }
    //Получение всех задач
    public List<TaskResponseDto> getAllTasks(){
        log.info("get all tasks");
        List<Task> taskList= taskRepository.findAll();
        var responseList = taskList.stream().map(t -> toResponse(t)).toList();
        return responseList;
    }

    //Создание Задачи и сохранение в базу данных
    public TaskResponseDto createTask(TaskRequestDto task){
        Task taskEntity = toEntity(task);
        return toResponse(taskRepository.save(taskEntity));
    }

    //Обновление задачи.
    @Transactional
    public TaskResponseDto updateTask(Long id, TaskRequestDto requestDto){
        Task entity = taskRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
        entity.setCreatorId(requestDto.creatorId());
        entity.setAssignedUserId(requestDto.assignedUserId());
        entity.setDeadlineDate(requestDto.deadlineDate());
        entity.setPriority(requestDto.priority());

        return toResponse(entity);
    }

    //Удаление задачи
    public TaskResponseDto deleteTask(Long id){

        Task task = taskRepository.findById(id).orElseThrow(() -> new NoSuchElementException(
                "Task not found with id: " + id
        ));
        taskRepository.delete(task);
        return toResponse(task);
    }


    // Вспомогательные методы для DTO
    private Task toEntity(TaskRequestDto request){
        return new Task(
                null,
                request.creatorId(),
                request.assignedUserId(),
                TaskStatus.CREATED,
                LocalDate.now(),
                request.deadlineDate(),
                request.priority()
        );
    }

    private TaskResponseDto toResponse(Task task){
        return new TaskResponseDto(
                task.getId(),
                task.getCreatorId(),
                task.getAssignedUserId(),
                task.getStatus(),
                task.getCreateDate(),
                task.getDeadlineDate(),
                task.getPriority()
        );
    }

}
