package com.example.taskManager.service;

import com.example.taskManager.dto.TaskDto.TaskCreateRequestDto;
import com.example.taskManager.dto.TaskDto.TaskResponseDto;
import com.example.taskManager.dto.TaskDto.TaskUpdateRequestDto;
import com.example.taskManager.enums.TaskStatus;
import com.example.taskManager.exception.*;
import com.example.taskManager.model.Tag;
import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final StudySessionRepository studySessionRepository;

    public TaskService(TaskRepository taskRepository, TagRepository tagRepository, UserRepository userRepository, StudySessionRepository studySessionRepository){
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;

        this.userRepository = userRepository;
        this.studySessionRepository = studySessionRepository;
    }


    //Получение задач из бд по ID
    public TaskResponseDto getTaskById(Long id, String email){
        log.info("get task by id");
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с такой почтой не найден."));
        Task task = taskRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));
        return toResponse(task);

    }

    //Получение всех задач
    public List<TaskResponseDto> getAllTasks(String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с такой почтой не найден"));
        Long id = user.getId();
        List<Task> taskList= taskRepository.findAllByUser_Id(id);
        var responseList = taskList.stream().map(this::toResponse).toList();
        return responseList;
    }

    //Создание Задачи и сохранение в базу данных
    @Transactional
    public TaskResponseDto createTask(TaskCreateRequestDto request, String email){
        if(request.tagId() == null){
            throw new InvalidRequestException("tagId is required");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден."));

        Tag tag = tagRepository.findById(request.tagId()).orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + request.tagId()));

        Long userId = (tag.getUser() != null) ? tag.getUser().getId() : null;

        if (userId == null || !userId.equals(user.getId())) {
            throw new InvalidRequestException(
                    "Tag does not belong to the user"
            );
        }

        Task taskEntity = toEntity(request, tag, user);

        return toResponse(taskRepository.save(taskEntity));
    }

    //Обновление задачи.
    @Transactional
    public TaskResponseDto updateTask(Long id, TaskUpdateRequestDto requestDto, String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Task entity = taskRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        if(requestDto.tagId() == null){
            throw new InvalidRequestException("tagId is required");
        }
        Tag tag = tagRepository.findById(requestDto.tagId()).orElseThrow(() -> new TagNotFoundException("Tag not found with id" + requestDto.tagId()));

        Long userIdInTheTag = (tag.getUser() != null) ? tag.getUser().getId() : null;
        Long userIdInTheTask = (entity.getUser() != null) ? entity.getUser().getId() : null;

        if(userIdInTheTask == null || userIdInTheTag == null || !userIdInTheTask.equals(userIdInTheTag)){
            throw new InvalidRequestException("Tag does not belong to the user");
        }

        entity.setDeadlineDate(requestDto.deadlineDate());
        entity.setPriority(requestDto.priority());
        entity.setTag(tag);
        entity.setStatus(requestDto.status());
        entity.setTitle(requestDto.title());

        return toResponse(entity);
    }

    //Удаление задачи
    @Transactional
    public TaskResponseDto deleteTask(Long id, String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));


        Task task = taskRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        if(studySessionRepository.existsByTaskId(task.getId())){
            throw new InvalidRequestException("Task is used by study sessions");
        }

        taskRepository.delete(task);
        return toResponse(task);
    }


    // Вспомогательные методы для DTO
    private Task toEntity(TaskCreateRequestDto request, Tag tag, User user){
        return new Task(
                null,
                user,
                request.title(),
                TaskStatus.CREATED,
                LocalDate.now(),
                request.deadlineDate(),
                request.priority(),
                tag
        );
    }

    private TaskResponseDto toResponse(Task task){
        Long tagId = (task.getTag() != null) ? task.getTag().getId() : null;
        String tagName = (task.getTag() != null) ? task.getTag().getName() : null;
        Long userId = (task.getUser() != null) ? task.getUser().getId() : null;
        return new TaskResponseDto(
                task.getId(),
                userId,
                task.getTitle(),
                task.getStatus(),
                task.getCreateDate(),
                task.getDeadlineDate(),
                task.getPriority(),
                tagId,
                tagName
        );
    }

}
