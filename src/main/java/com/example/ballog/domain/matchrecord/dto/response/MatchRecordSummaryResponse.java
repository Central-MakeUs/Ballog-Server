package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.matchrecord.entity.Result;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchRecordSummaryResponse {
    private Long matchRecordId;
    private Long matchesId;
    private String homeTeam;
    private String awayTeam;
    private String matchDate;
    private String matchTime;
    private Long userId;
    private Long watchCnt;
    private Result result;
    private BaseballTeam baseballTeam;
}
