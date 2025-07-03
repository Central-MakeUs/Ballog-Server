package com.example.ballog.domain.match.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.entity.Stadium;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class MatchesGroupedResponse {

    private Long matchesId;
    private LocalTime matchesTime;
    private BaseballTeam homeTeam;
    private BaseballTeam awayTeam;
    private Stadium stadium;
    private String matchesResult;

    public static MatchesGroupedResponse from(Matches match) {
        return new MatchesGroupedResponse(
                match.getMatchesId(),
                match.getMatchesTime(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getStadium(),
                match.getMatchesResult()
        );
    }
}
