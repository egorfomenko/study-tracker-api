package com.example.taskManager.repository;

import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    Optional<StudySession> findByUserIdAndStatus(Long userId, SessionStatus status);


    boolean existsByTagId(Long tagId);

    boolean existsByTaskId(Long taskId);


    List<StudySession> findAllByUserIdOrderByStartTimeDesc(Long userId);
}
