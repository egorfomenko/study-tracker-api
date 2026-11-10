package com.example.taskManager.controller;

import com.example.taskManager.dto.StudySessionDto.StudySessionRequestDto;
import com.example.taskManager.dto.StudySessionDto.StudySessionResponseDto;
import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.exception.ApiExceptionsHandler;
import com.example.taskManager.exception.ConflictExсeption;
import com.example.taskManager.service.StudySessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StudySessionControllerTest {
    @Test
    @DisplayName("POST /sessions/start возвращает 409, если у пользователя уже есть активная сессия")
    void shouldReturn409WhenUserAlreadyHasActiveSession() throws Exception {
        StudySessionService studySessionService = mock(StudySessionService.class);

        doThrow(new ConflictExсeption("Активная сессия уже существует"))
                .when(studySessionService)
                .startStudySession(any(StudySessionRequestDto.class));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new StudySessionController(studySessionService))
                .setControllerAdvice(new ApiExceptionsHandler())
                .build();

        mockMvc.perform(
                        post("/sessions/start")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {"userId": 42,
                        "tagId" : 1}
                        """)
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /sessions/start возвращает 400, если не передан tagId")
    void shouldReturn400WhenTagIdIsMissing() throws Exception {
        StudySessionService studySessionService = mock(StudySessionService.class);



        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new StudySessionController(studySessionService))
                .setControllerAdvice(new ApiExceptionsHandler())
                .build();

        mockMvc.perform(post("/sessions/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "userId" : 1
                        }
                        """)).andExpect(status().isBadRequest());

        verifyNoInteractions(studySessionService);
    }

    @Test
    @DisplayName("POST /sessions/start возвращает 201 и данные активной сессии")
    void shouldReturnCreatedSessionWhenStartSucceeds() throws Exception {
        StudySessionService studySessionService = mock(StudySessionService.class);
        StudySessionResponseDto studySessionResponseDto = new StudySessionResponseDto(
                10L,
                1L,
                1L,
                null,
                LocalDateTime.of(2026, 11, 10, 12, 0),
                null,
                null,
                SessionStatus.ACTIVE);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new StudySessionController(studySessionService))
                .build();

        when(studySessionService.startStudySession(any(StudySessionRequestDto.class)))
                .thenReturn( studySessionResponseDto);

        mockMvc.perform(post("/sessions/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "userId" : 1,
                            "tagId" : 1
                        }
                        """)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

    }


}
