package com.example.taskManager.dto.LiderboardDto;

public record LeaderboardEntryDto(
        Long userId,
        String name,
        Long totalDurationSeconds
) {}
