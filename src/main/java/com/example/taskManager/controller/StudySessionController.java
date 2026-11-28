package com.example.taskManager.controller;

import com.example.taskManager.dto.StudySessionDto.StudySessionRequestDto;
import com.example.taskManager.dto.StudySessionDto.StudySessionResponseDto;
import com.example.taskManager.service.StudySessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
public class StudySessionController {
    private final StudySessionService studySessionService;

    public StudySessionController(StudySessionService studySessionService) {
        this.studySessionService = studySessionService;
    }

    @GetMapping()
    public ResponseEntity<List<StudySessionResponseDto>> getAllSessionsByUserId(
            Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(studySessionService.getAllSessionsByUserId( email));
    }

    @PostMapping("/start")
    public ResponseEntity<StudySessionResponseDto> startSession (
            @RequestBody @Valid StudySessionRequestDto requestDto,
            Authentication authenticationl
            )
    {
        String email = authenticationl.getName();
        return ResponseEntity.status(201).body(studySessionService.startStudySession(requestDto, email));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<StudySessionResponseDto> stopSession(
            @PathVariable("id") Long id,
            Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(studySessionService.endStudySession(id, email));
    }
}
