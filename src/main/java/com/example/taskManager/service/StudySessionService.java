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
import org.springframework.dao.DataIntegrityViolationException;
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
    public StudySessionResponseDto startStudySession(StudySessionRequestDto requestDto, String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (requestDto.tagId() == null) {
            throw new InvalidRequestException("tagId is required");
        }



        Optional<StudySession> activeSession = studySessionRepository.
                findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE);

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

        try{
            StudySession sevedSession = studySessionRepository.saveAndFlush(session);
            return toResponse(sevedSession);
        }catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();

            if(cause instanceof org.hibernate.exception.ConstraintViolationException violation){
                if("23505".equals(violation.getSQLState()) && "study_sessions_one_active_per_user".equals(violation.getConstraintName())){
                    throw new ConflictExсeption("Индекс ограничивает создание второй активной сессии");
                }
            }
            throw e;
        }

    }
    @Transactional(readOnly = true)
    public List<StudySessionResponseDto> getAllSessionsByUserId(String email){
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new UserNotFoundException("Пользователь не найден")
        );

        List<StudySession> list = studySessionRepository.findAllByUserIdOrderByStartTimeDesc(user.getId());
        List<StudySessionResponseDto> listResponse = list.stream()
                .map(this::toResponse).toList();
        return listResponse;
    }

    @Transactional
    public StudySessionResponseDto endStudySession(Long sessionId, String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        StudySession session = studySessionRepository.findById(sessionId).
                orElseThrow(
                        () -> new SessionNotFoundException("Session not found with id" + sessionId)
                );
        if(!session.getUser().getId().equals(user.getId())){
            throw new SessionNotFoundException("Сессия с таким id не найдена");
        }

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





    //Вспомогательные метод
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
