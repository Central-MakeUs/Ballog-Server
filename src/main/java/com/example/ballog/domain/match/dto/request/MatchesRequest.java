package com.example.ballog.domain.match.dto.request;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Stadium;
import com.example.ballog.domain.match.entity.Status;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class MatchesRequest {
    private LocalDate matchesDate;
    private LocalTime matchesTime;
    private BaseballTeam homeTeam;
    private BaseballTeam awayTeam;
    private Stadium stadium;
    private String matchesResult;
    private Status status;
}