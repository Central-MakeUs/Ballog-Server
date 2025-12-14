package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.entity.Stadium;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.entity.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MatchRecordDetailResponse {
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
    private double positiveEmotionPercent;
    private double negativeEmotionPercent;
    private Long positiveCnt;
    private Long negativeCnt;
    private String defaultImageUrl;
    private List<ImageInfo> imageList;
    private List<EmotionGroupInfo> emotionGroupList;


    public static MatchRecordDetailResponse from(MatchRecord record,
                                                 double positiveEmotionPercent,
                                                 double negativeEmotionPercent,
                                                 long positiveCnt,
                                                 long negativeCnt,
                                                 List<ImageInfo> imageList,
                                                 List<EmotionGroupInfo> emotionGroups) {
        Matches match = record.getMatches();

        return new MatchRecordDetailResponse(
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
                record.getBaseballTeam(),
                positiveEmotionPercent,
                negativeEmotionPercent,
                positiveCnt,
                negativeCnt,
                imageList.isEmpty() ? null : imageList.get(0).getImageUrl(),
                imageList,
                emotionGroups
        );
    }
}
