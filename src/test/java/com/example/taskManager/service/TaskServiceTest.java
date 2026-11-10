package com.example.taskManager.service;

import com.example.taskManager.dto.TaskDto.TaskUpdateRequestDto;
import com.example.taskManager.enums.TaskPriority;
import com.example.taskManager.enums.TaskStatus;
import com.example.taskManager.exception.InvalidRequestException;
import com.example.taskManager.model.Tag;
import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock private TaskRepository taskRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserRepository userRepository;
    @Mock private StudySessionRepository studySessionRepository;

    @InjectMocks private TaskService taskService;

    @Test
    @DisplayName("Нельзя назначить задаче тег другого пользователя")
    void shouldRejectTaskUpdateWithAnotherUsersTag(){
        User user = new User(
                1L,
                "user",
                "user@mail.com"
        );
        User anotherUser = new User(
                2L,
                "anotherUser",
                "anotherUser@gmail.com"
        );
        Tag tag = new Tag(2L, "English", "0000", user);
        Tag anotherTag = new Tag(1L, "Java", "0000", anotherUser);

        Task task = new Task(
                1L,
                user,
                null,
                null,
                null,
                null,
                null,
                tag
        );

        TaskUpdateRequestDto request = new TaskUpdateRequestDto(
                "Java",
                TaskStatus.CREATED,
                LocalDate.now().plusWeeks(2),
                TaskPriority.Medium,
                1L
        );


        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
        when(tagRepository.findById(1L))
                .thenReturn(Optional.of(anotherTag));

        InvalidRequestException exception =  assertThrows(InvalidRequestException.class,
                () -> taskService.updateTask(1L, request));
        assertSame(tag, task.getTag());
        assertEquals("Tag does not belong to the user", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));

    }

}
