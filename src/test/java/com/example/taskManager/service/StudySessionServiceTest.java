package com.example.taskManager.service;

import com.example.taskManager.dto.StudySessionDto.StudySessionRequestDto;
import com.example.taskManager.dto.StudySessionDto.StudySessionResponseDto;
import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.exception.ConflictExсeption;
import com.example.taskManager.exception.InvalidRequestException;
import com.example.taskManager.exception.SessionNotFoundException;
import com.example.taskManager.model.StudySession;
import com.example.taskManager.model.Tag;
import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    @Test
    @DisplayName("успешный запуск сессии без задачи")
    void successfulSessionLaunchWithoutATask(){
        StudySessionService studySessionService = new StudySessionService(
                userRepository,
                studySessionRepository,
                tagRepository,
                taskRepository
        );

        StudySessionRequestDto studySessionRequestDto = new StudySessionRequestDto(
                1L,
                1L,
                null
        );

        User user = new User(
                studySessionRequestDto.userId(),
                "qwerty",
                "qwerty@gmail.com"
        );
        Tag tag = new Tag(
                1L,
                "Java",
                "0000",
                user
        );

        ArgumentCaptor<StudySession> argumentCaptor = ArgumentCaptor.forClass(StudySession.class);


        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(tagRepository.findById(1L))
                .thenReturn(Optional.of(tag));

        when(studySessionRepository.save(any(StudySession.class)))
                .thenAnswer(invocation -> {
                    StudySession savedSession = invocation.getArgument(0);
                    savedSession.setId(10L);
                    return savedSession;
                });
        when(studySessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        StudySessionResponseDto responseDto = studySessionService.startStudySession(studySessionRequestDto);

        verify(studySessionRepository).save(argumentCaptor.capture());

        StudySession session = argumentCaptor.getValue();

        assertEquals(10L, session.getId());
        assertEquals(user, session.getUser());
        assertEquals(tag, session.getTag());
        assertNull( session.getTask());
        assertEquals(SessionStatus.ACTIVE, session.getStatus());
        assertNotNull(session.getStartTime());
        assertNull(session.getEndTime());
        assertNull(session.getDurationSeconds());

        assertEquals(10L, responseDto.id());
        assertEquals(session.getUser().getId(), responseDto.userId());
        assertEquals(session.getTag().getId(), responseDto.tagId());
        assertNull(responseDto.taskId());
        assertEquals(session.getStartTime(), responseDto.startTime());
        assertNull(responseDto.endTime());
        assertNull(responseDto.durationSeconds());
        assertEquals(SessionStatus.ACTIVE, responseDto.status());

        verify(taskRepository, never()).findById(anyLong());

    }


    @Test
    @DisplayName("отказ запуска сессии с чужим тегом")
    void shouldRejectSessionStartWithAnotherUsersTag(){
        StudySessionService studySessionService = new StudySessionService(
                userRepository,
                studySessionRepository,
                tagRepository,
                taskRepository
        );

        User userA = new User(
                1L,
                "qwerty",
                "qwerty@gmail.com"
        );
        User userB = new User(2L, null, null);

        Tag tag = new Tag(
                1L,
                "Java",
                "0000",
                userB
        );

        StudySessionRequestDto request = new StudySessionRequestDto(
                1L,
                1L,
                null
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userA));
        when(tagRepository.findById(1L))
                .thenReturn(Optional.of(tag));


        assertThrows(InvalidRequestException.class,
                () -> studySessionService.startStudySession(request));

        verify(studySessionRepository, never()).save(any(StudySession.class));



    }

}
