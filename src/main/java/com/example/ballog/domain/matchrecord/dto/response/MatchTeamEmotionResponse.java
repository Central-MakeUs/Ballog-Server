package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.emotion.entity.EmotionType;
import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Matches;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

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
    private long totalPositiveCnt;
    private long totalNegativeCnt;

    // NONE일 경우 (각 팀별 긍정/부정 퍼센테이지 및 대표 감정)
    private EmotionType homeTeamEmotion;
    private Double homeTeamPercent;
    private EmotionType awayTeamEmotion;
    private Double awayTeamPercent;

    public static MatchTeamEmotionResponse forUserTeam(
            Matches match,
            BaseballTeam userTeam,
            long positiveCnt,
            long negativeCnt
    ) {
        MatchTeamEmotionResponse r = base(match);
        r.userTeam = userTeam.name();
        r.totalPositiveCnt = positiveCnt;
        r.totalNegativeCnt = negativeCnt;
        return r;
    }

    public static MatchTeamEmotionResponse forNoTeam(
            Matches match,
            EmotionType homeEmotion,
            double homePercent,
            EmotionType awayEmotion,
            double awayPercent
    ) {
        MatchTeamEmotionResponse r = base(match);
        r.userTeam = BaseballTeam.NONE.name();
        r.homeTeamEmotion = homeEmotion;
        r.homeTeamPercent = homePercent;
        r.awayTeamEmotion = awayEmotion;
        r.awayTeamPercent = awayPercent;
        return r;
    }

    private static MatchTeamEmotionResponse base(Matches match) {
        MatchTeamEmotionResponse r = new MatchTeamEmotionResponse();
        r.matchId = match.getMatchesId();
        r.stadium = match.getStadium().name();
        r.homeTeam = match.getHomeTeam().name();
        r.awayTeam = match.getAwayTeam().name();
        r.matchDate = match.getMatchesDate();
        r.matchTime = match.getMatchesTime();
        return r;
    }
}
