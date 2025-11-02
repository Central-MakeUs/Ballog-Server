package com.example.ballog.domain.matchrecord.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class MatchTeamEmotionResponse {
    private Long matchId;
    private String stadium;
    private String homeTeam;
    private String awayTeam;
    private LocalDate matchDate;
    private LocalTime matchTime;

    private String userTeam;

    //사용자가 팀을 응원하는 경우
    private Double positiveEmotionPercent;
    private Double negativeEmotionPercent;
    private List<EmotionGroupInfo> emotionGroupList;

    //NONE일 경우 (각 팀별)
    private Double homeTeamPositivePercent;
    private Double homeTeamNegativePercent;
    private Double awayTeamPositivePercent;
    private Double awayTeamNegativePercent;
}

