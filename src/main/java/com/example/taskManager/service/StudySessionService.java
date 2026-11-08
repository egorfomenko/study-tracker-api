package com.example.taskManager.service;


import com.example.taskManager.dto.StudySessionDto.StudySessionRequestDto;
import com.example.taskManager.dto.StudySessionDto.StudySessionResponseDto;
import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.exception.*;
import com.example.taskManager.model.StudySession;
import com.example.taskManager.model.Tag;
import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudySessionService {
    private final UserRepository userRepository;
    private final StudySessionRepository studySessionRepository;
    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;

    public StudySessionService(UserRepository userRepository, StudySessionRepository studySessionRepository, TagRepository tagRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.studySessionRepository = studySessionRepository;
        this.tagRepository = tagRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public StudySessionResponseDto startStudySession(StudySessionRequestDto requestDto){

        if (requestDto.userId() == null) {
            throw new InvalidRequestException("userId is required");
        }
        if (requestDto.tagId() == null) {
            throw new InvalidRequestException("tagId is required");
        }

        User user = userRepository.findById(requestDto.userId()).orElseThrow(
                    () -> new UserNotFoundException("User not found with id" + requestDto.userId())
            );

        Optional<StudySession> activeSession = studySessionRepository.
                findByUserIdAndStatus(requestDto.userId(), SessionStatus.ACTIVE);

        if(activeSession.isPresent()){
            throw new ConflictExсeption("User already has an active study session");
        }

        Tag tag = tagRepository.findById(requestDto.tagId()).orElseThrow(
                    () -> new TagNotFoundException("Tag not found with id" + requestDto.tagId())
            );
        if (tag.getUser() == null
                || !tag.getUser().getId().equals(user.getId())) {
            throw new InvalidRequestException(
                    "Tag does not belong to the user"
            );
        }

        Task task = null;

        if(requestDto.taskId() != null){

            task = taskRepository.findById(requestDto.taskId()).orElseThrow(
                    () -> new TaskNotFoundException("Task not found with id" + requestDto.taskId())
            );
            //Проверка тегов на совпадение (чтобы нельзя было запустить tag другого пользователя)
            Long userIdInTag = (tag.getUser() != null) ? tag.getUser().getId() : null;
            Long userIdInTask = (task.getUser() != null) ? task.getUser().getId() : null;

            if(userIdInTask == null || userIdInTag == null || !userIdInTag.equals(userIdInTask)){
                throw new InvalidRequestException("Task does not belong to the user");
            }


            Long tagId = tag.getId();
            Long tagIdInTheTask = (task.getTag() != null) ? task.getTag().getId() : null;


            if(tagId == null || tagIdInTheTask == null || !tagId.equals(tagIdInTheTask)){
                    throw new InvalidRequestException("Task does not belong to the selected tag");
            }
        }

        StudySession session = new StudySession(
                null,
                user,
                tag,
                task,
                LocalDateTime.now(),
                null,
                null,
                SessionStatus.ACTIVE
        );

        return toResponse(studySessionRepository.save(session));

    }
    @Transactional(readOnly = true)
    public List<StudySessionResponseDto> getAllSessionsByUserId(Long id){
        if(id == null){
            throw new InvalidRequestException("userId is required");
        }
        userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found with id" + id)
        );

        List<StudySession> list = studySessionRepository.findAllByUserIdOrderByStartTimeDesc(id);
        List<StudySessionResponseDto> listResponse = list.stream()
                .map(this::toResponse).toList();
        return listResponse;
    }

    @Transactional
    public StudySessionResponseDto endStudySession(Long sessionId){
        StudySession session = studySessionRepository.findById(sessionId).
                orElseThrow(
                        () -> new SessionNotFoundException("Session not found with id" + sessionId)
                );

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new ConflictExсeption("Session is not active");
        }

        session.setEndTime(LocalDateTime.now());
        session.setDurationSeconds(Duration.between(
                session.getStartTime(), session.getEndTime()
        ).getSeconds());
        session.setStatus(SessionStatus.COMPLETED);

        return toResponse(session);
    }



    //Вспомогательные методы
    private StudySessionResponseDto toResponse(StudySession session){
        Long userId = (session.getUser() != null) ? session.getUser().getId() : null;
        Long tadId = (session.getTag() != null) ? session.getTag().getId() : null;
        Long taskId = (session.getTask() != null) ? session.getTask().getId() : null;
        return new StudySessionResponseDto(
                session.getId(),
                userId,
                tadId,
                taskId,
                session.getStartTime(),
                session.getEndTime(),
                session.getDurationSeconds(),
                session.getStatus()
        );
    }

}
