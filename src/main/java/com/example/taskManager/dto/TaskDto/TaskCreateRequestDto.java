package com.example.taskManager.dto.TaskDto;

import com.example.taskManager.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;


public record TaskCreateRequestDto (
        Long userId,
        @NotBlank String title,
        LocalDate deadlineDate,
        TaskPriority priority,
        Long tagId
) {
}
