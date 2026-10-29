package com.example.taskManager.dto;

import com.example.taskManager.enums.TaskPriority;
import com.example.taskManager.enums.TaskStatus;

import java.time.LocalDate;

public record TaskResponseDto(
        Long id,
        Long creatorId,
        Long assignedUserId,
        TaskStatus status,
        LocalDate createDate,
        LocalDate deadlineDate,
        TaskPriority priority,
        Long tagId,
        String tagName
) {
}
