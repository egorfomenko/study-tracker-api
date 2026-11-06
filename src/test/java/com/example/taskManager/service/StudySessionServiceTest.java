package com.example.taskManager.service;

import com.example.taskManager.dto.StudySessionDto.StudySessionRequestDto;
import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.exception.ConflictExсeption;
import com.example.taskManager.model.StudySession;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class StudySessionServiceTest {

    @Test
    void StudtSessionServiceTest(){
        UserRepository userRepository =  mock(UserRepository.class);
        StudySessionRepository studySessionRepository = mock(StudySessionRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);


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
}
