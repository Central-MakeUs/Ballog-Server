package com.example.ballog.domain.match.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.entity.Stadium;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class MatchesResponse {
    private Long matchesId;
    private LocalDate matchesDate;
    private LocalTime matchesTime;
    private BaseballTeam homeTeam;
    private BaseballTeam awayTeam;
    private Stadium stadium;
    private String matchesResult;

    public static MatchesResponse from(Matches match) {
        return new MatchesResponse(
                match.getMatchesId(),
                match.getMatchesDate(),
                match.getMatchesTime(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getStadium(),
                match.getMatchesResult()
        );
    }
}
