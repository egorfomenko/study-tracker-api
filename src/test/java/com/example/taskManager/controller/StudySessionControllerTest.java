package com.example.taskManager.controller;

import com.example.taskManager.dto.StudySessionDto.StudySessionRequestDto;
import com.example.taskManager.exception.ApiExceptionsHandler;
import com.example.taskManager.exception.ConflictExсeption;
import com.example.taskManager.service.StudySessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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


}
