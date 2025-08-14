package com.example.ballog.domain.match.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.entity.Stadium;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class MatchesResponse {
    private Long matchesId;
    private LocalDate matchesDate;
    private String  matchesTime;
    private BaseballTeam homeTeam;
    private BaseballTeam awayTeam;
    private Stadium stadium;
    private String matchesResult;

    public static MatchesResponse from(Matches match) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return new MatchesResponse(
                match.getMatchesId(),
                match.getMatchesDate(),
                match.getMatchesTime().format(formatter),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getStadium(),
                match.getMatchesResult()
        );
    }
}
