package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.entity.Result;
import com.example.ballog.domain.login.entity.BaseballTeam;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class MatchRecordResponse {
    private Long matchRecordId;
    private Long matchesId;
    private BaseballTeam homeTeam;
    private BaseballTeam awayTeam;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private Long userId;
    private Long watchCnt;
    private Result result;
    private BaseballTeam baseballTeam;

    private double positiveEmotionPercent;
    private double negativeEmotionPercent;

    private String defaultImageUrl;

    public static MatchRecordResponse from(MatchRecord record, Matches match) {
        return new MatchRecordResponse(
                record.getMatchrecordId(),
                match.getMatchesId(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getMatchesDate(),
                match.getMatchesTime(),
                record.getUser().getUserId(),
                record.getWatchCnt(),
                record.getResult(),
                record.getBaseballTeam(),
                0.0, // positiveEmotionPercent (기본값)
                0.0, // negativeEmotionPercent (기본값)
                record.getDefaultImageUrl()
        );
    }

}
