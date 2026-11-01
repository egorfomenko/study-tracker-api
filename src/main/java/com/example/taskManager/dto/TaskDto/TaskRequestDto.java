package com.example.taskManager.dto.TaskDto;

import com.example.taskManager.enums.TaskPriority;

import java.time.LocalDate;

public record TaskRequestDto(
        Long userId,
        LocalDate deadlineDate,
        TaskPriority priority,
        Long tagId
) {
}
