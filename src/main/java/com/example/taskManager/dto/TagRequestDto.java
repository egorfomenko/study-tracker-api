package com.example.taskManager.dto;

public record TagRequestDto (
        String name,
        String colorHex,
        Long userId
){
}
