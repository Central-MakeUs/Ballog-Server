package com.example.ballog.domain.friend.dto;

import com.example.ballog.domain.baseball.entity.BaseballTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class FriendDto {
    private Long userId;
    private String nickname;
    private BaseballTeam baseballTeam;
    private Long positiveCnt;
    private Long negativeCnt;

    public FriendDto(Long userId, String nickname, BaseballTeam baseballTeam, Long positiveCnt, Long negativeCnt) {
        this.userId = userId;
        this.nickname = nickname;
        this.baseballTeam = baseballTeam;
        this.positiveCnt = positiveCnt == null ? 0L : positiveCnt;
        this.negativeCnt = negativeCnt == null ? 0L : negativeCnt;
    }

    public String getEmotion() {
        long total = positiveCnt + negativeCnt;

        if (total == 0) return "NEUTRAL";

        double ratio = (double) positiveCnt / total;

        if (ratio >= 0.6) return "POSITIVE";
        if (ratio <= 0.4) return "NEGATIVE";
        return "NEUTRAL";
    }
}