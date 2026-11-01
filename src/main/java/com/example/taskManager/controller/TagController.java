package com.example.taskManager.controller;

import com.example.taskManager.dto.TagDto.TagCreateRequestDto;
import com.example.taskManager.dto.TagDto.TagResponseDto;
import com.example.taskManager.dto.TagDto.TagUpdateRequestDto;
import com.example.taskManager.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<TagResponseDto>>getAllTags(){
        return ResponseEntity.status(200).body(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTagById(
           @PathVariable("id") Long id
    ){
        return ResponseEntity.status(200).body(tagService.getTagById(id));
    }

    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(
           @Valid @RequestBody TagCreateRequestDto tag
    ){
        return ResponseEntity.status(201).body(tagService.createTag(tag));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> updateTag(
            @PathVariable("id") Long id,
            @Valid @RequestBody TagUpdateRequestDto request
    ){
        return ResponseEntity.status(200).body(tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagResponseDto> deleteTag(
            @PathVariable Long id
    ){
        return ResponseEntity.status(200).body(tagService.deleteTag(id));
    }
}
