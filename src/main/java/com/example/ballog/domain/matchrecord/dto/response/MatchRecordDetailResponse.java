package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Stadium;
import com.example.ballog.domain.matchrecord.entity.Result;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
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
    private String defaultImageUrl;
    private List<ImageInfo> imageList;

    @Getter
    @Builder
    public static class ImageInfo {
        private String imageUrl;
        private LocalDateTime createdAt;
    }
}
