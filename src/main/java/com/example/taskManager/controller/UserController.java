package com.example.taskManager.controller;

import com.example.taskManager.dto.StudySessionDto.StudySessionResponseDto;
import com.example.taskManager.dto.TagDto.TagResponseDto;
import com.example.taskManager.dto.UserDto.UserRequestDto;
import com.example.taskManager.dto.UserDto.UserResponseDto;
import com.example.taskManager.service.StudySessionService;
import com.example.taskManager.service.TagService;
import com.example.taskManager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final TagService tagService;
    private final StudySessionService studySessionService;

    public UserController(UserService userService, TagService tagService, StudySessionService studySessionService){
        this.userService = userService;
        this.tagService = tagService;
        this.studySessionService = studySessionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.status(200).body(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        return ResponseEntity.status(200).body(userService.getAllUsers());
    }

    @GetMapping("/{id}/tags")
    public ResponseEntity<List<TagResponseDto>> getAllTagsByUserId(
            @PathVariable Long id
    ){
        return ResponseEntity.status(200).body(tagService.getAllTagsByUserId(id));
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<List<StudySessionResponseDto>> getAllSessionsByUserId(
            @PathVariable Long id
    ){
        return ResponseEntity.status(200).body(studySessionService.getAllSessionsByUserId(id));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @RequestBody UserRequestDto user
    ){
        return ResponseEntity.status(201).body(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable("id") Long id,
            @RequestBody UserRequestDto userRequestDto
    ){
        return ResponseEntity.status(200).body(userService.updateUser(id, userRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDto> deleteUser(
            @PathVariable("id")
            Long id
    ){
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
