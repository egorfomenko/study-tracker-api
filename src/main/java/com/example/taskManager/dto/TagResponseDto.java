package com.example.taskManager.dto;

public record TagResponseDto (
        Long id,
        String name,
        String colorHex,
        Long userId
) {
}
