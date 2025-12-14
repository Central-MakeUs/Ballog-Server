package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.entity.Stadium;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.entity.Result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class MatchRecordSummaryResponse {
    private Long matchRecordId;
    private Long matchesId;
    private Stadium stadium;
    private BaseballTeam homeTeam;
    private BaseballTeam awayTeam;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private Long userId;
    private Long watchCnt;
    private Result result;
    private BaseballTeam baseballTeam;

    public static MatchRecordSummaryResponse from(MatchRecord record) {
        Matches match = record.getMatches();
        return new MatchRecordSummaryResponse(
                record.getMatchrecordId(),
                match.getMatchesId(),
                match.getStadium(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getMatchesDate(),
                match.getMatchesTime(),
                record.getUser().getUserId(),
                record.getWatchCnt(),
                record.getResult(),
                record.getBaseballTeam()
        );
    }
}
