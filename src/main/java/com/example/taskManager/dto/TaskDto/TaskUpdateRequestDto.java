package com.example.taskManager.dto.TaskDto;

import com.example.taskManager.enums.TaskPriority;
import com.example.taskManager.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;


public record TaskUpdateRequestDto (
        @NotBlank String title,
        @NotNull TaskStatus status,
        LocalDate deadlineDate,
        TaskPriority priority,
        @NotNull @Positive Long tagId
){
}
