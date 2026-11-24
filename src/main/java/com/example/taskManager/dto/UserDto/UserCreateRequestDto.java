package com.example.taskManager.dto.UserDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequestDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
