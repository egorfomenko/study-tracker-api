package com.example.taskManager.repository;


import com.example.taskManager.dto.LiderboardDto.LeaderboardEntryDto;
import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.model.StudySession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaderboardRepository extends JpaRepository<StudySession, Long> {
    @Query("""
            select new com.example.taskManager.dto.LiderboardDto.LeaderboardEntryDto (s.user.id, s.user.name, sum(s.durationSeconds)) from StudySession s
            where s.status = :status
            group by s.user.id, s.user.name
            order by sum(s.durationSeconds) desc, s.user.id asc
            """)
    List<LeaderboardEntryDto> findLeaderboard(
            @Param("status") SessionStatus status,
            Pageable pageable
    );

}
