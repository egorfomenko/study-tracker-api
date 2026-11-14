package com.example.taskManager.controller;

import com.example.taskManager.dto.LiderboardDto.LeaderboardEntryDto;
import com.example.taskManager.service.LiderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LiderboardController {
    private final LiderboardService liderboardService;

    @Autowired
    public LiderboardController(LiderboardService liderboardService) {
        this.liderboardService = liderboardService;
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLiders(){
        return ResponseEntity.status(200).body(liderboardService.getLeaders());
    }

}
