package com.example.taskManager.dto.TagDto;

import jakarta.validation.constraints.NotBlank;

public record TagUpdateRequestDto(
        @NotBlank String name,
        String colorHex
) {
}
