package com.example.taskManager.dto;

import com.example.taskManager.enums.TaskPriority;

import java.time.LocalDateTime;

public record TaskRequestDto(
        Long creatorId,
        Long assignedUserId,
        LocalDateTime deadlineDate,
        TaskPriority priority
) {
}
