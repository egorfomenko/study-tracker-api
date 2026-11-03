package com.example.taskManager.dto.TaskDto;

import com.example.taskManager.enums.TaskPriority;

import java.time.LocalDate;


public record TaskUpdateRequestDto (
        LocalDate deadlineDate,
        TaskPriority priority,
        Long tagId
){
}
