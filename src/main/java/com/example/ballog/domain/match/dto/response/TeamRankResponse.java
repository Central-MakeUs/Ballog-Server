package com.example.ballog.domain.match.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TeamRankResponse {

    private String teamCode;
    private int rank;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime updatedAt;

    private double positiveRate;
    private double negativeRate;
}
