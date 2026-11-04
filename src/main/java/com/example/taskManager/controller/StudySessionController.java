package com.example.taskManager.controller;

import com.example.taskManager.dto.StudySessionDto.StudySessionRequestDto;
import com.example.taskManager.dto.StudySessionDto.StudySessionResponseDto;
import com.example.taskManager.service.StudySessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions")
public class StudySessionController {
    private final StudySessionService studySessionService;

    public StudySessionController(StudySessionService studySessionService) {
        this.studySessionService = studySessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<StudySessionResponseDto> startSession (
            @RequestBody @Valid StudySessionRequestDto requestDto
            )
    {
        return ResponseEntity.status(201).body(studySessionService.startStudySession(requestDto));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<StudySessionResponseDto> stopSession(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.status(200).body(studySessionService.endStudySession(id));
    }
}
