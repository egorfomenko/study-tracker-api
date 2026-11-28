package com.example.taskManager.service;

import com.example.taskManager.exception.InvalidRequestException;
import com.example.taskManager.model.Tag;
import com.example.taskManager.repository.StudySessionRepository;
import com.example.taskManager.repository.TagRepository;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock private StudySessionRepository studySessionRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private TagRepository tagRepository;

    @InjectMocks TagService tagService;

    @Test
    @DisplayName("Нельзя удалить тег, который используется в учебной сессии")
    @Disabled("Доделаю, когда допишу сервис")
    void shouldRejectTagDeletionWhenUsedByStudySessions() {
        Tag tag = new Tag(
                1L,
                "Java",
                "0000",
                null
        );


        when(tagRepository.findById(1L))
                .thenReturn(Optional.of(tag));

        when(studySessionRepository.existsByTagId(1L))
                .thenReturn(true);

//        assertThrows(InvalidRequestException.class,
//                () -> tagService.deleteTag(1L, ));
        verify(tagRepository, never()).delete(any(Tag.class));

    }
}
