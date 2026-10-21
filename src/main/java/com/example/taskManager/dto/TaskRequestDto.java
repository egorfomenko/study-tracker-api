package com.example.taskManager.dto;

import com.example.taskManager.enums.TaskPriority;

import java.time.LocalDate;

public record TaskRequestDto(
        Long creatorId,
        Long assignedUserId,
        LocalDate deadlineDate,
        TaskPriority priority
) {
}
