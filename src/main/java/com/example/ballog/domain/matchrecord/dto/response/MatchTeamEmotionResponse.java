package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Matches;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class MatchTeamEmotionResponse {

    private Long matchId;
    private String stadium;
    private String homeTeam;
    private String awayTeam;
    private LocalDate matchDate;
    private LocalTime matchTime;

    private String userTeam;

    // 사용자가 팀을 응원하는 경우
    private Double positiveEmotionPercent;
    private Double negativeEmotionPercent;
    private List<EmotionGroupInfo> emotionGroupList;

    // NONE일 경우 (각 팀별)
    private Double homeTeamPositivePercent;
    private Double homeTeamNegativePercent;
    private Double awayTeamPositivePercent;
    private Double awayTeamNegativePercent;

    public static MatchTeamEmotionResponse from(
            Matches match,
            BaseballTeam userTeam,
            Double userPositivePercent,
            Double userNegativePercent,
            List<EmotionGroupInfo> userTeamEmotionGroups,
            Double homePositivePercent,
            Double homeNegativePercent,
            Double awayPositivePercent,
            Double awayNegativePercent
    ) {
        MatchTeamEmotionResponse response = new MatchTeamEmotionResponse();
        response.matchId = match.getMatchesId();
        response.stadium = match.getStadium().name();
        response.homeTeam = match.getHomeTeam().name();
        response.awayTeam = match.getAwayTeam().name();
        response.matchDate = match.getMatchesDate();
        response.matchTime = match.getMatchesTime();

        if (userTeam != null && userTeam != BaseballTeam.NONE) {  //사용자가 응원하는 팀이 있는 경우
            response.userTeam = userTeam.name();
            response.positiveEmotionPercent = userPositivePercent;
            response.negativeEmotionPercent = userNegativePercent;
            response.emotionGroupList = userTeamEmotionGroups;
        } else { //사용자가 응원하는 팀이 없을 경우 -> 해당 경기의 홈팀과 상대팀의 퍼센테이지를 보여준다.
            response.userTeam = "NONE";
            response.homeTeamPositivePercent = homePositivePercent;
            response.homeTeamNegativePercent = homeNegativePercent;
            response.awayTeamPositivePercent = awayPositivePercent;
            response.awayTeamNegativePercent = awayNegativePercent;
        }

        return response;
    }
}
