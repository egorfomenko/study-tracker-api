package com.example.taskManager.service;

import com.example.taskManager.dto.LiderboardDto.LeaderboardEntryDto;
import com.example.taskManager.enums.SessionStatus;
import com.example.taskManager.repository.LeaderboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiderboardService {
    private final LeaderboardRepository leaderboardRepository;

    @Autowired
    public LiderboardService(LeaderboardRepository leaderboardRepository) {
        this.leaderboardRepository = leaderboardRepository;
    }

    public List<LeaderboardEntryDto> getLeaders(){
        List<LeaderboardEntryDto> leaders = leaderboardRepository.findLeaderboard(SessionStatus.COMPLETED,  Pageable.ofSize(10));
        return leaders;
    }
}
