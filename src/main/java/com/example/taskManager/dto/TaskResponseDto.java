package com.example.taskManager.dto;

import com.example.taskManager.enums.TaskPriority;
import com.example.taskManager.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponseDto(
        Long id,
        Long creatorId,
        Long assignedUserId,
        TaskStatus status,
        LocalDateTime createDateTime,
        LocalDateTime deadlineDate,
        TaskPriority priority
) {
}
