package com.example.ballog.domain.emotion.dto.response;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.entity.Stadium;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class EmotionResponse {
    private LocalDate matchesDate;
    private LocalTime matchesTime;
    private BaseballTeam homeTeam;
    private BaseballTeam awayTeam;
    private Stadium stadium;

    private double positivePercent;
    private double negativePercent;
}
