package com.example.taskManager.dto.StudySessionDto;

public record StudySessionRequestDto (
        Long id, Long userId, Long tagId, Long taskId
){
}
