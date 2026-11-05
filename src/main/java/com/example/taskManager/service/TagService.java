package com.example.taskManager.service;

import com.example.taskManager.dto.TagDto.TagCreateRequestDto;
import com.example.taskManager.dto.TagDto.TagResponseDto;
import com.example.taskManager.dto.TagDto.TagUpdateRequestDto;
import com.example.taskManager.exception.InvalidRequestException;
import com.example.taskManager.exception.TagNotFoundException;
import com.example.taskManager.exception.TaskNotFoundException;
import com.example.taskManager.exception.UserNotFoundException;
import com.example.taskManager.model.Tag;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TagService {
    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final StudySessionRepository studySessionRepository;

    public TagService(TaskRepository taskRepository, TagRepository tagRepository, UserRepository userRepository, StudySessionRepository studySessionRepository) {
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.studySessionRepository = studySessionRepository;
    }

    public List<TagResponseDto> getAllTags(){
        List<Tag> list = tagRepository.findAll().stream().toList();
        List<TagResponseDto> listResponse = list.stream().map(
                tag -> toResponse(tag)
        ).toList();

        return listResponse;
    }

    public TagResponseDto getTagById(Long id){
        Tag tag = tagRepository.findById(id).orElseThrow(
                () -> new TagNotFoundException("Tag not found with id" + id)
        );

        return toResponse(tag);
    }

    public List<TagResponseDto> getAllTagsByUserId(Long id) {
        if(id == null){
            throw new InvalidRequestException("userId is required");
        }
        userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found with id" + id));


        List<Tag> tags = tagRepository.findAllByUserId(id);
        List<TagResponseDto> tagsResponse = tags.stream().map(
                this::toResponse
        ).toList();
        return tagsResponse;
    }

    @Transactional
    public TagResponseDto createTag(TagCreateRequestDto request){
        if(request.userId() == null){
            throw new InvalidRequestException("userId is required");
        }

        User user = userRepository.findById(request.userId()).orElseThrow(
                    () -> new UserNotFoundException(
                            "User not found with id: " + request.userId()
                    )
            );


        Tag tag = toEntity(request, user);
        tag = tagRepository.save(tag);

        return toResponse(tag);
    }

    @Transactional
    public TagResponseDto updateTag(Long id, TagUpdateRequestDto request){
        Tag tag = tagRepository.findById(id).orElseThrow(
                () -> new TagNotFoundException("Tag not found with id" + id)
        );

        tag.setName(request.name());
        tag.setColorHex(request.colorHex());

        return toResponse(tag);
    }

    @Transactional
    public TagResponseDto deleteTag(Long id){
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));
        if(taskRepository.existsByTagId(tag.getId()) || studySessionRepository.existsByTagId(tag.getId())){
            throw new InvalidRequestException("Can't delete this tag. It has connections");
        }
        tagRepository.delete(tag);
        return toResponse(tag);

    }


    //Вспомогательные методы
    private TagResponseDto toResponse(Tag tag){
        Long userId = (tag.getUser() != null) ? tag.getUser().getId() : null;

        return new TagResponseDto(
                tag.getId(),
                tag.getName(),
                tag.getColorHex(),
                userId
        );
    }

    private Tag toEntity(TagCreateRequestDto tagRequestDto, User user){

        return new Tag(
                null,
                tagRequestDto.name(),
                tagRequestDto.colorHex(),
                user
        );
    }
}
