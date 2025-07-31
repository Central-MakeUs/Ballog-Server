package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Stadium;
import com.example.ballog.domain.matchrecord.entity.Result;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
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
}
