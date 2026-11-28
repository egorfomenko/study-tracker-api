package com.example.taskManager.dto.StudySessionDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StudySessionRequestDto (
        @NotNull  @Positive Long tagId,
        @Positive Long taskId
){
}
