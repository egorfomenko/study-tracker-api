package com.example.taskManager.controller;

import com.example.taskManager.dto.TagDto.TagCreateRequestDto;
import com.example.taskManager.dto.TagDto.TagResponseDto;
import com.example.taskManager.dto.TagDto.TagUpdateRequestDto;
import com.example.taskManager.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService){
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagResponseDto>>getAllTags(
            Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(tagService.getAllTags(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTagById(
           @PathVariable("id") Long id,
           Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(tagService.getTagById(id,email));
    }

    @GetMapping("{tagId}/stats")
    public ResponseEntity<Long> getAlldurationSeconds(
            Authentication authentication,
            @PathVariable("tagId") Long tagId
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(tagService.getAlldurationSeconds(email, tagId));
    }

    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(
           @Valid @RequestBody TagCreateRequestDto tag,
           Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(201).body(tagService.createTag(tag, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> updateTag(
            @PathVariable("id") Long id,
            @Valid @RequestBody TagUpdateRequestDto request,
            Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(tagService.updateTag(id, request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagResponseDto> deleteTag(
            @PathVariable Long id,
            Authentication authentication
    ){
        String email = authentication.getName();
        return ResponseEntity.status(200).body(tagService.deleteTag(id, email));
    }
}
