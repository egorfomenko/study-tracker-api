package com.example.taskManager.service;

import com.example.taskManager.dto.StudySessionDto.StudySessionRequestDto;
import com.example.taskManager.dto.StudySessionDto.StudySessionResponseDto;
import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.exception.ConflictExсeption;
import com.example.taskManager.exception.SessionNotFoundException;
import com.example.taskManager.model.StudySession;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.core.Local;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StudySessionServiceTest {

    private UserRepository userRepository;
    private StudySessionRepository studySessionRepository;
    private  TagRepository tagRepository;
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp(){
        userRepository =  mock(UserRepository.class);
        studySessionRepository = mock(StudySessionRepository.class);
        tagRepository = mock(TagRepository.class);
        taskRepository = mock(TaskRepository.class);
    }


    @Test
    @DisplayName("Проверка запуска второго таймера, пока активен первый.")
    void studySessionStartTest(){

        StudySessionService studySessionService = new StudySessionService(
                userRepository,
                studySessionRepository,
                tagRepository,
                taskRepository
        );


        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        StudySessionRequestDto request = new StudySessionRequestDto(
                user.getId(),
                1L,
                null
        );

        StudySession session = new StudySession(
                null,
                user,
                null,
                null,
                null,
                null,
                null,
                SessionStatus.ACTIVE
        );

        when(studySessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.of(session));

        assertThrows(ConflictExсeption.class, () -> studySessionService.startStudySession(request));

        verify(studySessionRepository, never()).save(any(StudySession.class));
    }


    @Test
    @DisplayName("Запрещает повторную остановку завершённой сессии")
    void studySessionStopTest(){
        StudySessionService studySessionService = new StudySessionService(
                userRepository,
                studySessionRepository,
                tagRepository,
                taskRepository
        );

        LocalDateTime startTime = LocalDateTime.of(2026, 11, 7, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(30);
        Long durationSeconds = 1800L;


        StudySession session = new StudySession(
                null,
                null,
                null,
                null,
                startTime,
                endTime,
                durationSeconds,
                SessionStatus.COMPLETED
        );



        when(studySessionRepository.findById(1L))
                .thenReturn(Optional.of(session));



        assertThrows(ConflictExсeption.class, () -> studySessionService.endStudySession(1L));

        assertEquals(endTime, session.getEndTime());
        assertEquals(durationSeconds, session.getDurationSeconds());
        assertEquals(SessionStatus.COMPLETED, session.getStatus());

    }

    @Test
    @DisplayName("Завершает активную сессию и рассчитывает длительность")
    void sessionFinishSuccessful(){
        StudySessionService studySessionService = new StudySessionService(
                userRepository,
                studySessionRepository,
                tagRepository,
                taskRepository
        );
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);


        StudySession session = new StudySession(
                1L,
                null,
                null,
                null,
                startTime,
                null,
                null,
                SessionStatus.ACTIVE
        );


        when(studySessionRepository.findById(1L))
                .thenReturn(Optional.of(session));

        LocalDateTime beforeCall = LocalDateTime.now();

        StudySessionResponseDto responseDto = studySessionService.endStudySession(1L);

        LocalDateTime afterCall = LocalDateTime.now();

        assertFalse(session.getEndTime().isBefore(beforeCall));
        assertFalse(session.getEndTime().isAfter(afterCall));
        assertEquals(Duration.between(startTime, session.getEndTime()).getSeconds(), session.getDurationSeconds());
        assertEquals(SessionStatus.COMPLETED, session.getStatus());



        assertEquals(1L, responseDto.id());
        assertEquals(SessionStatus.COMPLETED, responseDto.status());
        assertEquals(session.getEndTime(), responseDto.endTime());
        assertEquals(session.getDurationSeconds(), responseDto.durationSeconds());

    }

    @Test
    @DisplayName("Остановка несуществующей сессии")
    void stoppingANonExistentSession(){
        StudySessionService studySessionService = new StudySessionService(
                userRepository,
                studySessionRepository,
                tagRepository,
                taskRepository
        );

        when(studySessionRepository.findById(1L))
                .thenReturn(Optional.empty());


        assertThrows(SessionNotFoundException.class,
                () -> studySessionService.endStudySession(1L));

    }





}
