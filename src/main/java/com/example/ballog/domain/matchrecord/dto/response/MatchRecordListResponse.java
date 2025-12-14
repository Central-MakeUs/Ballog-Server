package com.example.ballog.domain.matchrecord.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MatchRecordListResponse {
    private int totalCount;
    private double winRate;
    private double totalPositiveEmotionPercent;
    private double totalNegativeEmotionPercent;
    private List<MatchRecordSummaryResponse> records;

    public static MatchRecordListResponse from(
            int totalCount,
            double winRate,
            double positivePercent,
            double negativePercent,
            List<MatchRecordSummaryResponse> recordResponses
    ) {
        return MatchRecordListResponse.builder()
                .totalCount(totalCount)
                .winRate(winRate)
                .totalPositiveEmotionPercent(positivePercent)
                .totalNegativeEmotionPercent(negativePercent)
                .records(recordResponses)
                .build();
    }
}
