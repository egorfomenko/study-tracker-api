package com.example.taskManager.controller;

import com.example.taskManager.dto.StudySessionDto.StudySessionResponseDto;
import com.example.taskManager.dto.UserDto.UserCreateRequestDto;
import com.example.taskManager.dto.UserDto.UserResponseDto;
import com.example.taskManager.service.StudySessionService;
import com.example.taskManager.service.TagService;
import com.example.taskManager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final StudySessionService studySessionService;

    public UserController(UserService userService, TagService tagService, StudySessionService studySessionService){
        this.userService = userService;
        this.studySessionService = studySessionService;
    }

    @GetMapping("/me")
    public UserResponseDto getCurrentUser(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName());
    }






    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @RequestBody @Valid UserCreateRequestDto user
    ){
        return ResponseEntity.status(201).body(userService.createUser(user));
    }

}
