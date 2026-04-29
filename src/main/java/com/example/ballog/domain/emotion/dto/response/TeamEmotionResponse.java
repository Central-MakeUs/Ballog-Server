package com.example.ballog.domain.emotion.dto.response;

import com.example.ballog.domain.baseball.entity.BaseballTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamEmotionResponse {

    private BaseballTeam team;
    private long positiveCount;
    private long negativeCount;

    public double getPositiveRate() {
        long total = positiveCount + negativeCount;
        return total == 0 ? 0.0 : (positiveCount * 100.0) / total;
    }

    public double getNegativeRate() {
        long total = positiveCount + negativeCount;
        return total == 0 ? 0.0 : (negativeCount * 100.0) / total;
    }
}