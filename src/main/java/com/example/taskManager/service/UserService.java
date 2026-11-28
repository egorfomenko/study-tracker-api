package com.example.taskManager.service;

import com.example.taskManager.dto.UserDto.UserCreateRequestDto;
import com.example.taskManager.dto.UserDto.UserResponseDto;
import com.example.taskManager.exception.UserNotFoundException;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.taskManager.exception.ConflictExсeption;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TagRepository tagRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(this::toResponse)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }


    public UserResponseDto createUser(UserCreateRequestDto userRequestDto){
        User user = toEntity(userRequestDto);
        try {
            return toResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException error) {
            for (Throwable cause = error; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "ux_users_email_normalized".equals(violation.getConstraintName())) {
                    throw new ConflictExсeption("Этот email уже зарегистрирован. Войди в аккаунт.");
                }
            }
            throw error;
        }
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
