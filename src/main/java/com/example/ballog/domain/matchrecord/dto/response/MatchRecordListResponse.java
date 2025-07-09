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
}
