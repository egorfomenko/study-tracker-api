package com.example.taskManager.dto.TaskDto;

import com.example.taskManager.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;


public record TaskCreateRequestDto (
        @NotNull @Positive Long userId,
        @NotBlank String title,
        LocalDate deadlineDate,
        TaskPriority priority,
        @NotNull @Positive Long tagId
) {
}
