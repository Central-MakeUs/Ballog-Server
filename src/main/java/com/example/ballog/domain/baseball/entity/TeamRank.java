package com.example.ballog.domain.baseball.entity;

import java.time.LocalDateTime;

public class TeamRank {
    private int rank;
    private LocalDateTime updatedAt;

    public TeamRank(int rank, LocalDateTime updatedAt) {
        this.rank = rank;
        this.updatedAt = updatedAt;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Rank: " + rank + ", UpdatedAt: " + updatedAt;
    }
}
