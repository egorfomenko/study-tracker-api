package com.example.taskManager.service;

import com.example.taskManager.dto.UserRequestDto;
import com.example.taskManager.dto.UserResponseDto;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDto getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        return toResponse(user);
    }

    public List<UserResponseDto> getAllUsers(){
        List<User> list = userRepository.findAll();
        List<UserResponseDto> responseDtoList = list.stream().map(
                this::toResponse
        ).toList();
        return responseDtoList;
    }

    public UserResponseDto createUser(UserRequestDto userRequestDto){
        User user = toEntity(userRequestDto);
        return toResponse(userRepository.save(user));
    }
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto){
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        user.setEmail(userRequestDto.email());
        user.setName(userRequestDto.name());
        return toResponse(user);
    }

    public UserResponseDto deleteUser(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        userRepository.delete(user);
        return toResponse(user);
    }

    //Вспомогательные классы для UserService
    private User toEntity(UserRequestDto userRequestDto){
        return new User(
                null,
                userRequestDto.name(),
                userRequestDto.email()
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
