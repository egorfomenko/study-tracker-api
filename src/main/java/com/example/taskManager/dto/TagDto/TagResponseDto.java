package com.example.taskManager.dto.TagDto;

public record TagResponseDto (
        Long id,
        String name,
        String colorHex,
        Long userId
) {
}
