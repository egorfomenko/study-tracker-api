package com.example.taskManager.repository;

import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    Optional<StudySession> findByUserIdAndStatus(Long userId, SessionStatus status);

    boolean existsByTagId(Long tagId);

    boolean existsByTaskId(Long taskId);

    List<StudySession> findAllByUserIdOrderByStartTimeDesc(Long userId);

    @Query("""
            select coalesce(sum(s.durationSeconds), 0)  from StudySession s
            where s.user.id = :userId and s.tag.id = :tagId and s.status = :status
            """)
    Long sumDurationByUserAndTag(
            @Param("userId") Long userId,
            @Param("tagId") Long tagId,
            @Param("status") SessionStatus status
    );
}
