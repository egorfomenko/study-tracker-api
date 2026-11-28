package com.example.taskManager.dto.TagDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TagCreateRequestDto(
        @NotBlank String name,
        String colorHex
){
}
