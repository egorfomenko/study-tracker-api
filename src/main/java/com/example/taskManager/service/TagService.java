package com.example.taskManager.service;

import com.example.taskManager.dto.TagDto.TagCreateRequestDto;
import com.example.taskManager.dto.TagDto.TagResponseDto;
import com.example.taskManager.dto.TagDto.TagUpdateRequestDto;
import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.exception.*;
import com.example.taskManager.model.Tag;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<TagResponseDto> getAllTags(String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        List<Tag> list = tagRepository.findAllByUserId(user.getId()).stream().toList();

        List<TagResponseDto> listResponse = list.stream().map(
                tag -> toResponse(tag)
        ).toList();

        return listResponse;
    }

    public TagResponseDto getTagById(Long id, String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));



        Tag tag = tagRepository.findByIdAndUser_Id(id, user.getId()).orElseThrow(
                () -> new TagNotFoundException("Tag not found with id" + id)
        );

        return toResponse(tag);
    }

    public Long getAlldurationSeconds(String email, Long tagId){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        tagRepository.findByIdAndUser_Id(tagId, user.getId())
                .orElseThrow(() -> new TagNotFoundException("Тег не найден"));

        return studySessionRepository.sumDurationByUserAndTag(user.getId(), tagId, SessionStatus.COMPLETED);
    }

    @Transactional
    public TagResponseDto createTag(TagCreateRequestDto request, String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if(user.getId() == null){
            throw new InvalidRequestException("userId is required");
        }

        Tag tag = toEntity(request, user);
        tag = tagRepository.save(tag);

        return toResponse(tag);
    }

    @Transactional
    public TagResponseDto updateTag(Long id, TagUpdateRequestDto request, String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Tag tag = tagRepository.findByIdAndUser_Id(id, user.getId()).orElseThrow(
                () -> new TagNotFoundException("Tag not found with id" + id)
        );

        tag.setName(request.name());
        tag.setColorHex(request.colorHex());

        return toResponse(tag);
    }

    @Transactional
    public TagResponseDto deleteTag(Long id,String email){
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Tag tag = tagRepository.findByIdAndUser_Id(id,user.getId())
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));
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
