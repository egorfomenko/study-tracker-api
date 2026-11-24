package com.example.taskManager.service;

import com.example.taskManager.dto.UserDto.UserCreateRequestDto;
import com.example.taskManager.dto.UserDto.UserResponseDto;
import com.example.taskManager.dto.UserDto.UserUpdateRequestDto;
import com.example.taskManager.exception.UserNotFoundException;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TagRepository tagRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    public List<UserResponseDto> getAllUsers(){
        List<User> list = userRepository.findAll();
        List<UserResponseDto> responseDtoList = list.stream().map(
                this::toResponse
        ).toList();
        return responseDtoList;
    }


    public UserResponseDto createUser(UserCreateRequestDto userRequestDto){
        User user = toEntity(userRequestDto);
        return toResponse(userRepository.save(user));
    }
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto userRequestDto){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setEmail(userRequestDto.email());
        user.setName(userRequestDto.name());
        return toResponse(user);
    }

    public UserResponseDto deleteUser(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        return toResponse(user);
    }

    //Вспомогательные классы для UserService
    private User toEntity(UserCreateRequestDto userRequestDto){
        return new User(
                null,
                userRequestDto.name(),
                userRequestDto.email(),
                passwordEncoder.encode(userRequestDto.password())
        );
    }

    private UserResponseDto toResponse(User user){
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }


}
