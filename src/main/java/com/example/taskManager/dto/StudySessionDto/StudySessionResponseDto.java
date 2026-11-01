package com.example.taskManager.dto.StudySessionDto;

import com.example.taskManager.enums.SessionStatus;

import java.time.LocalDateTime;

public record StudySessionResponseDto(
        Long id,
        Long userId,
        Long tagId,
        Long taskId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long durationSeconds,
        SessionStatus status
) {
}
